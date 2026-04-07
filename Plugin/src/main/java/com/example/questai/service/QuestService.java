package com.example.questai.service;

import com.example.questai.db.DBConnector;
import com.example.questai.generator.QuestGenerator;
import com.example.questai.ml.*;
import com.example.questai.model.Quest;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.example.questai.model.QuestCandidate;
import com.example.questai.ml.QuestDTO;
import com.example.questai.ml.PlayerDTO;

public class QuestService {

    private final DBConnector db;
    private final MLClient mlClient;

    public QuestService(DBConnector db) {
        this.db = db;
        this.mlClient = new MLClient();
    }

    public int createQuest(Player player) throws SQLException {

        int playerId = db.getPlayerId(player.getUniqueId().toString());

        if (playerId == -1) {
            db.savePlayer(player.getUniqueId().toString(), player.getName());
            playerId = db.getPlayerId(player.getUniqueId().toString());
        }

        // Получаем успешность из БД (теперь из таблицы players)
        double successRate = db.getSuccessRate(playerId);
        
        // Получаем kills и deaths из статистики Bukkit
        int kills = player.getStatistic(org.bukkit.Statistic.MOB_KILLS);
        int deaths = player.getStatistic(org.bukkit.Statistic.DEATHS);

        // ===== СОЗДАЁМ ОБЪЕКТ PLAYER ДЛЯ ГЕНЕРАТОРА =====
        com.example.questai.model.Player gamePlayer = new com.example.questai.model.Player();
        gamePlayer.setId(playerId);
        gamePlayer.setUuid(player.getUniqueId().toString());
        gamePlayer.setName(player.getName());
        gamePlayer.setKills(kills);
        gamePlayer.setDeaths(deaths);
        gamePlayer.setSuccessRate(successRate);
        // =============================================

        List<QuestCandidate> candidates = new ArrayList<>();
        List<Quest> candidateQuests = new ArrayList<>();

        // Генерация 5 кандидатов с передачей Player
        for (int i = 0; i < 5; i++) {
            Quest q = QuestGenerator.generateQuest(gamePlayer);
            QuestDTO dto = new QuestDTO(q.getAmount());
            candidates.add(new QuestCandidate(q, dto));
            candidateQuests.add(q);
        }

        // ===== ЛОГИРОВАНИЕ КАНДИДАТОВ =====
        System.out.println("--- CANDIDATES ---");
        for (int i = 0; i < candidates.size(); i++) {
            Quest q = candidates.get(i).getQuest();
            System.out.println("Candidate " + i + ": " + 
                               "type=" + q.getType() + 
                               ", amount=" + q.getAmount() + 
                               ", reward=" + q.getReward());
        }
        // =================================

        List<QuestDTO> dtoList = candidates.stream()
                .map(QuestCandidate::getDto)
                .toList();

        PlayerDTO playerDTO = new PlayerDTO(deaths, kills, successRate);

        RankRequest request = new RankRequest(playerDTO, dtoList);

        System.out.println("--- ML REQUEST ---");
        System.out.println("Player: deaths=" + deaths +
                ", kills=" + kills +
                ", successRate=" + successRate);

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

        int bestIndex = chooseBestQuest(results);

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
                           ", amount=" + bestQuest.getAmount() + 
                           ", reward=" + bestQuest.getReward());
        System.out.println("ML Score: " + bestScore);

        // ===== СОХРАНЕНИЕ С НОВОЙ СТРУКТУРОЙ =====
        int questId = db.saveQuest(playerId, bestQuest, gamePlayer, bestScore, true);
        
        // Сохраняем ML предсказания для всех кандидатов
        if (results != null && !results.isEmpty()) {
            for (RankResponse r : results) {
                db.saveMlPrediction(questId, r.getQuestIndex(), r.getScore(), r.getQuestIndex() == bestIndex);
            }
        }
        // =======================================

        return questId;
    }

    private int chooseBestQuest(List<RankResponse> responses) {

        if (responses == null || responses.isEmpty()) {
            return -1;
        }

        RankResponse best = null;

        for (RankResponse r : responses) {
            if (best == null || r.getScore() > best.getScore()) {
                best = r;
            }
        }

        return best != null ? best.getQuestIndex() : -1;
    }

    public void completeQuest(Player player, int questId) throws SQLException {

        int deathsAfter = player.getStatistic(org.bukkit.Statistic.DEATHS);
        int killsAfter = player.getStatistic(org.bukkit.Statistic.MOB_KILLS);
        
        db.completeQuest(questId, deathsAfter, killsAfter);
        
        System.out.println("[QuestService] Quest " + questId + " completed! " +
                           "Deaths: " + deathsAfter + ", Kills: " + killsAfter);
    }

    public void failQuest(Player player, int questId) throws SQLException {

        int deathsAfter = player.getStatistic(org.bukkit.Statistic.DEATHS);
        int killsAfter = player.getStatistic(org.bukkit.Statistic.MOB_KILLS);
        
        db.failQuest(questId, deathsAfter, killsAfter);
        
        System.out.println("[QuestService] Quest " + questId + " failed! " +
                           "Deaths: " + deathsAfter + ", Kills: " + killsAfter);
    }
}