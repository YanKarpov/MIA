package com.example.questai.service;

import com.example.questai.db.DBConnector;
import com.example.questai.generator.QuestGenerator;
import com.example.questai.ml.*;
import com.example.questai.model.Quest;
import org.bukkit.entity.Player;
import org.bukkit.Location;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.example.questai.model.QuestCandidate;
import com.example.questai.ml.QuestDTO;
import com.example.questai.ml.PlayerDTO;

public class QuestService {

    private final DBConnector db;
    private final MLClient mlClient;
    private final int candidatesCount;
    private final double mlThreshold;

    public QuestService(DBConnector db, int candidatesCount, double mlThreshold) {
        this.db = db;
        this.mlClient = new MLClient();
        this.candidatesCount = candidatesCount;
        this.mlThreshold = mlThreshold;
    }

    public int createQuest(Player player) throws SQLException {

        int playerId = db.getPlayerId(player.getUniqueId().toString());

        if (playerId == -1) {
            db.savePlayer(player.getUniqueId().toString(), player.getName());
            playerId = db.getPlayerId(player.getUniqueId().toString());
        }

        double successRate = db.getSuccessRate(playerId);
        
        int kills = player.getStatistic(org.bukkit.Statistic.MOB_KILLS);
        int deaths = player.getStatistic(org.bukkit.Statistic.DEATHS);
        
        Location location = player.getLocation();

        com.example.questai.model.Player gamePlayer = new com.example.questai.model.Player();
        gamePlayer.setId(playerId);
        gamePlayer.setUuid(player.getUniqueId().toString());
        gamePlayer.setName(player.getName());
        gamePlayer.setKills(kills);
        gamePlayer.setDeaths(deaths);
        gamePlayer.setSuccessRate(successRate);
        
        gamePlayer.setLastQuestType(getLastQuestType(playerId));
        gamePlayer.setConsecutiveSuccesses(getConsecutiveSuccesses(playerId));
        gamePlayer.setFavoriteType(getFavoriteQuestType(playerId));
        gamePlayer.setLeastFavoriteType(getLeastFavoriteQuestType(playerId));
        gamePlayer.setPreferredTarget(getPreferredTarget(playerId));

        // Используем количество кандидатов из конфига
        List<QuestCandidate> candidates = new ArrayList<>();

        for (int i = 0; i < candidatesCount; i++) {
            Quest q = QuestGenerator.generateQuest(gamePlayer, location);
            QuestDTO dto = new QuestDTO(q.getAmount());
            candidates.add(new QuestCandidate(q, dto));
        }

        System.out.println("--- CANDIDATES (" + candidatesCount + ") ---");
        for (int i = 0; i < candidates.size(); i++) {
            Quest q = candidates.get(i).getQuest();
            System.out.println("Candidate " + i + ": " + 
                               "type=" + q.getType() + 
                               ", target=" + q.getTarget() +
                               ", amount=" + q.getAmount() + 
                               ", reward=" + q.getReward());
        }

        List<QuestDTO> dtoList = candidates.stream()
                .map(QuestCandidate::getDto)
                .toList();

        PlayerDTO playerDTO = new PlayerDTO(deaths, kills, successRate);

        RankRequest request = new RankRequest(playerDTO, dtoList);

        System.out.println("--- ML REQUEST ---");
        System.out.println("Player: deaths=" + deaths +
                ", kills=" + kills +
                ", successRate=" + successRate);
        System.out.println("Biome: " + location.getWorld().getBiome(location.getBlockX(), location.getBlockZ()));

        List<RankResponse> results = mlClient.rank(request);

        System.out.println("--- ML RESPONSE ---");

        if (results == null || results.isEmpty()) {
            System.out.println("ML returned EMPTY or NULL");
        } else {
            for (RankResponse r : results) {
                System.out.println("Quest index: " + r.getQuestIndex() +
                        " | score: " + r.getScore());
            }
        }

        // Применяем порог для выбора лучшего кандидата
        int bestIndex = chooseBestQuestWithThreshold(results, candidates);

        System.out.println("=== SELECTED QUEST ===");
        System.out.println("Best index: " + bestIndex);

        if (bestIndex == -1) {
            bestIndex = 0;
        }

        Quest bestQuest = candidates.get(bestIndex).getQuest();
        double bestScore = results != null && bestIndex < results.size() 
            ? results.get(bestIndex).getScore() 
            : 0.5;
        
        System.out.println("Selected: type=" + bestQuest.getType() + 
                           ", target=" + bestQuest.getTarget() +
                           ", amount=" + bestQuest.getAmount() + 
                           ", reward=" + bestQuest.getReward());
        System.out.println("ML Score: " + bestScore);
        System.out.println("Threshold used: " + mlThreshold);

        int questId = db.saveQuest(playerId, bestQuest, gamePlayer, bestScore, true);
        
        // Сохраняем ВСЕХ кандидатов с их деталями
        if (results != null && !results.isEmpty()) {
            for (RankResponse r : results) {
                QuestCandidate candidate = candidates.get(r.getQuestIndex());
                Quest quest = candidate.getQuest();
                db.saveMlPrediction(questId, r.getQuestIndex(), r.getScore(), 
                                    r.getQuestIndex() == bestIndex, quest);
            }
        }
        
        db.updateLastQuestType(playerId, bestQuest.getType());

        return questId;
    }

    private int chooseBestQuestWithThreshold(List<RankResponse> responses, List<QuestCandidate> candidates) {
        if (responses == null || responses.isEmpty()) {
            int easiestIndex = 0;
            for (int i = 1; i < candidates.size(); i++) {
                if (candidates.get(i).getQuest().getAmount() < candidates.get(easiestIndex).getQuest().getAmount()) {
                    easiestIndex = i;
                }
            }
            System.out.println("Fallback: выбран лёгкий квест (amount=" + 
                               candidates.get(easiestIndex).getQuest().getAmount() + ")");
            return easiestIndex;
        }

        // Фильтруем кандидатов по порогу
        List<Integer> validIndices = new ArrayList<>();
        for (int i = 0; i < responses.size(); i++) {
            if (responses.get(i).getScore() >= mlThreshold) {
                validIndices.add(i);
            }
        }
        
        System.out.println("Threshold filter: " + validIndices.size() + "/" + responses.size() + 
                           " candidates passed (threshold=" + mlThreshold + ")");
        
        if (validIndices.isEmpty()) {
            // Если все ниже порога — берём лучшего
            System.out.println("No candidates passed threshold, selecting best overall");
            RankResponse best = responses.get(0);
            int bestIndex = 0;
            for (int i = 1; i < responses.size(); i++) {
                if (responses.get(i).getScore() > best.getScore()) {
                    best = responses.get(i);
                    bestIndex = i;
                }
            }
            return bestIndex;
        }
        
        // Выбираем лучшего среди прошедших порог
        int bestIndex = validIndices.get(0);
        double bestScore = responses.get(bestIndex).getScore();
        for (int idx : validIndices) {
            if (responses.get(idx).getScore() > bestScore) {
                bestScore = responses.get(idx).getScore();
                bestIndex = idx;
            }
        }
        
        return bestIndex;
    }

    private int chooseBestQuest(List<RankResponse> responses, List<QuestCandidate> candidates) {
        if (responses == null || responses.isEmpty()) {
            int easiestIndex = 0;
            for (int i = 1; i < candidates.size(); i++) {
                if (candidates.get(i).getQuest().getAmount() < candidates.get(easiestIndex).getQuest().getAmount()) {
                    easiestIndex = i;
                }
            }
            System.out.println("Fallback: выбран лёгкий квест (amount=" + 
                               candidates.get(easiestIndex).getQuest().getAmount() + ")");
            return easiestIndex;
        }

        RankResponse best = null;
        for (RankResponse r : responses) {
            if (best == null || r.getScore() > best.getScore()) {
                best = r;
            }
        }
        return best != null ? best.getQuestIndex() : -1;
    }

    private String getLastQuestType(int playerId) throws SQLException {
        return db.getLastQuestType(playerId);
    }
    
    private int getConsecutiveSuccesses(int playerId) throws SQLException {
        return db.getConsecutiveSuccesses(playerId);
    }
    
    private String getFavoriteQuestType(int playerId) throws SQLException {
        return db.getFavoriteQuestType(playerId);
    }
    
    private String getLeastFavoriteQuestType(int playerId) throws SQLException {
        return db.getLeastFavoriteQuestType(playerId);
    }
    
    private String getPreferredTarget(int playerId) throws SQLException {
        return db.getPreferredTarget(playerId);
    }

    public void completeQuest(Player player, int questId) throws SQLException {
        int deathsAfter = player.getStatistic(org.bukkit.Statistic.DEATHS);
        int killsAfter = player.getStatistic(org.bukkit.Statistic.MOB_KILLS);
        
        String questType = db.getQuestType(questId);
        
        db.completeQuest(questId, deathsAfter, killsAfter);
        
        db.updatePlayerQuestHistory(questId, true);
        
        updatePlayerSuccessRate(questId);
        
        System.out.println("[QuestService] Quest " + questId + " completed! " +
                           "Type: " + questType +
                           ", Deaths: " + deathsAfter + ", Kills: " + killsAfter);
    }

    public void failQuest(Player player, int questId) throws SQLException {
        int deathsAfter = player.getStatistic(org.bukkit.Statistic.DEATHS);
        int killsAfter = player.getStatistic(org.bukkit.Statistic.MOB_KILLS);
        
        String questType = db.getQuestType(questId);
        
        db.failQuest(questId, deathsAfter, killsAfter);
        
        db.updatePlayerQuestHistory(questId, false);
        
        updatePlayerSuccessRate(questId);
        
        System.out.println("[QuestService] Quest " + questId + " failed! " +
                           "Type: " + questType +
                           ", Deaths: " + deathsAfter + ", Kills: " + killsAfter);
    }
    
    /**
     * Обновляет success rate игрока на основе истории квестов
     * @param questId ID завершённого квеста
     * @throws SQLException если ошибка БД
     */
    private void updatePlayerSuccessRate(int questId) throws SQLException {
        int playerId = db.getPlayerIdByQuestId(questId);
        
        db.updatePlayerStats(playerId);
        
        double newSuccessRate = db.getSuccessRate(playerId);
        
        System.out.println("[QuestService] Updated success rate for player " + playerId + 
                           ": " + String.format("%.2f", newSuccessRate));
    }
}