package com.example.questai.service;

import com.example.questai.db.DBConnector;
import com.example.questai.model.Player;
import com.example.questai.model.QuestCandidate;
import com.example.questai.ml.PlayerDTO;
import com.example.questai.ml.QuestDTO;
import com.example.questai.ml.RankResponse;
import org.bukkit.Location;

import java.sql.SQLException;
import java.util.List;

public class QuestService {

    private final PlayerService playerService;
    private final CandidateGenerator candidateGenerator;
    private final MLRankingService mlRankingService;
    private final QuestStore questStore;

    public QuestService(DBConnector db) {
        this.playerService = new PlayerService(db);
        this.candidateGenerator = new CandidateGenerator();
        this.mlRankingService = new MLRankingService();
        this.questStore = new QuestStore(db);
    }

    public int createQuest(org.bukkit.entity.Player bukkitPlayer) throws SQLException {
        int playerId = playerService.getOrCreatePlayerId(bukkitPlayer);
        Player gamePlayer = playerService.buildGamePlayer(playerId, bukkitPlayer);
        Location location = bukkitPlayer.getLocation();
        
        List<QuestCandidate> candidates = candidateGenerator.generate(gamePlayer, location);
        candidateGenerator.logCandidates(candidates);
        
        List<QuestDTO> dtoList = candidates.stream()
                .map(QuestCandidate::getDto)
                .toList();
        PlayerDTO playerDTO = new PlayerDTO(gamePlayer.getDeaths(), gamePlayer.getKills(), gamePlayer.getSuccessRate());
        
        mlRankingService.logRequest(playerDTO, location);
        List<RankResponse> results = mlRankingService.rank(playerDTO, dtoList);
        mlRankingService.logResponse(results);
        
        int bestIndex = mlRankingService.getBestIndex(results);
        if (bestIndex == -1) bestIndex = 0;
        
        com.example.questai.model.Quest bestQuest = candidates.get(bestIndex).getQuest();
        double bestScore = mlRankingService.getBestScore(results, bestIndex);
        
        System.out.println("=== SELECTED QUEST ===");
        System.out.println("Best index: " + bestIndex);
        System.out.println("Selected: type=" + bestQuest.getType() + 
                           ", target=" + bestQuest.getTarget() +
                           ", amount=" + bestQuest.getAmount() + 
                           ", reward=" + bestQuest.getReward());
        System.out.println("ML Score: " + bestScore);
        
        int questId = questStore.saveSelectedQuest(playerId, bestQuest, gamePlayer, bestScore);
        questStore.saveAllPredictions(questId, candidates, results, bestIndex);
        questStore.updateLastQuestType(playerId, bestQuest.getType());
        
        return questId;
    }

    public void completeQuest(org.bukkit.entity.Player bukkitPlayer, int questId) throws SQLException {
        int deathsAfter = bukkitPlayer.getStatistic(org.bukkit.Statistic.DEATHS);
        int killsAfter = bukkitPlayer.getStatistic(org.bukkit.Statistic.MOB_KILLS);
        
        String questType = questStore.getQuestType(questId);
        
        questStore.completeQuest(questId, deathsAfter, killsAfter);
        questStore.updatePlayerQuestHistory(questId, true);
        updatePlayerSuccessRate(questId);
        
        System.out.println("[QuestService] Quest " + questId + " completed! " +
                           "Type: " + questType +
                           ", Deaths: " + deathsAfter + ", Kills: " + killsAfter);
    }

    public void failQuest(org.bukkit.entity.Player bukkitPlayer, int questId) throws SQLException {
        int deathsAfter = bukkitPlayer.getStatistic(org.bukkit.Statistic.DEATHS);
        int killsAfter = bukkitPlayer.getStatistic(org.bukkit.Statistic.MOB_KILLS);
        
        String questType = questStore.getQuestType(questId);
        
        questStore.failQuest(questId, deathsAfter, killsAfter);
        questStore.updatePlayerQuestHistory(questId, false);
        updatePlayerSuccessRate(questId);
        
        System.out.println("[QuestService] Quest " + questId + " failed! " +
                           "Type: " + questType +
                           ", Deaths: " + deathsAfter + ", Kills: " + killsAfter);
    }
    
    private void updatePlayerSuccessRate(int questId) throws SQLException {
        int playerId = questStore.getPlayerIdByQuestId(questId);
        questStore.updatePlayerStats(playerId);
        double newSuccessRate = questStore.getSuccessRate(playerId);
        
        System.out.println("[QuestService] Updated success rate for player " + playerId + 
                           ": " + String.format("%.2f", newSuccessRate));
    }
}