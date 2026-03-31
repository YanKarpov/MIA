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

        double successRate = db.getSuccessRate(playerId);

        List<QuestCandidate> candidates = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
            Quest q = QuestGenerator.generateQuest();
            QuestDTO dto = new QuestDTO(q.getAmount());
            candidates.add(new QuestCandidate(q, dto));
        }

        List<QuestDTO> dtoList = candidates.stream()
                .map(QuestCandidate::getDto)
                .toList();

        PlayerDTO playerDTO = new PlayerDTO(
                player.getStatistic(org.bukkit.Statistic.DEATHS),
                player.getStatistic(org.bukkit.Statistic.MOB_KILLS),
                successRate
        );

        RankRequest request = new RankRequest(playerDTO, dtoList);

        System.out.println("--- ML REQUEST ---");
        System.out.println("Player: deaths=" + playerDTO.getDeaths() +
                ", kills=" + playerDTO.getKills() +
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

        return db.saveQuest(
                playerId,
                bestQuest.getType(),
                bestQuest.getTarget(),
                bestQuest.getAmount(),
                bestQuest.getReward()
        );
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

        db.completeQuest(
                questId,
                player.getStatistic(org.bukkit.Statistic.DEATHS),
                player.getStatistic(org.bukkit.Statistic.MOB_KILLS)
        );
    }

    public void failQuest(Player player, int questId) throws SQLException {

        db.failQuest(
                questId,
                player.getStatistic(org.bukkit.Statistic.DEATHS),
                player.getStatistic(org.bukkit.Statistic.MOB_KILLS)
        );
    }
}