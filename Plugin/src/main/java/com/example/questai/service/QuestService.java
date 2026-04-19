package com.example.questai.service;

import com.example.questai.db.DBConnector;
import com.example.questai.model.Quest;
import com.example.questai.model.QuestCandidate;
import com.example.questai.ml.RankResponse;
import org.bukkit.entity.Player;
import org.bukkit.Location;

import java.sql.SQLException;
import java.util.List;

public class QuestService {

    private final PlayerService playerService;
    private final CandidateGenerator candidateGenerator;
    private final MLRankingService mlRankingService;
    private final QuestStore questStore;
    private final DBConnector db;

    public QuestService(DBConnector db) {
        this.db = db;
        this.playerService = new PlayerService(db);
        this.candidateGenerator = new CandidateGenerator();
        this.mlRankingService = new MLRankingService();
        this.questStore = new QuestStore(db);
    }

    public int createQuest(Player player) throws SQLException {
        int playerId = playerService.getOrCreatePlayerId(player);
        
        com.example.questai.model.Player gamePlayer = playerService.buildGamePlayer(playerId, player);
        Location location = player.getLocation();
        
        int candidatesCount = db.getCandidatesCount();
        double mlThreshold = db.getMlThreshold();
        
        List<QuestCandidate> candidates = candidateGenerator.generate(gamePlayer, location, candidatesCount);
        candidateGenerator.logCandidates(candidates);
        
        List<RankResponse> results = mlRankingService.rankWithCandidates(candidates, gamePlayer, location);
        
        int bestIndex = mlRankingService.chooseBestWithThreshold(results, candidates, mlThreshold);
        
        Quest bestQuest = candidates.get(bestIndex).getQuest();
        double bestScore = mlRankingService.getScoreByIndex(results, bestIndex);
        
        int questId = questStore.saveSelectedQuest(playerId, bestQuest, gamePlayer, bestScore);
        questStore.saveAllPredictions(questId, candidates, results, bestIndex);
        questStore.updateLastQuestType(playerId, bestQuest.getType());
        
        return questId;
    }

    public void completeQuest(Player player, int questId) throws SQLException {
        int deathsAfter = player.getStatistic(org.bukkit.Statistic.DEATHS);
        int killsAfter = player.getStatistic(org.bukkit.Statistic.MOB_KILLS);
        
        questStore.completeQuest(questId, deathsAfter, killsAfter);
        questStore.updatePlayerQuestHistory(questId, true);
        
        updatePlayerSuccessRate(questId);
    }

    public void failQuest(Player player, int questId) throws SQLException {
        int deathsAfter = player.getStatistic(org.bukkit.Statistic.DEATHS);
        int killsAfter = player.getStatistic(org.bukkit.Statistic.MOB_KILLS);
        
        questStore.failQuest(questId, deathsAfter, killsAfter);
        questStore.updatePlayerQuestHistory(questId, false);
        
        updatePlayerSuccessRate(questId);
    }
    
    private void updatePlayerSuccessRate(int questId) throws SQLException {
        int playerId = questStore.getPlayerIdByQuestId(questId);
        questStore.updatePlayerStats(playerId);
        
        double newSuccessRate = questStore.getSuccessRate(playerId);
        System.out.println("[QuestService] Updated success rate for player " + playerId + 
                           ": " + String.format("%.2f", newSuccessRate));
    }
}