package com.example.questai.service;

import com.example.questai.db.DBConnector;
import com.example.questai.generator.QuestGenerator;
import com.example.questai.model.Quest;
import org.bukkit.entity.Player;

import java.sql.SQLException;

public class QuestService {

    private final DBConnector db;

    public QuestService(DBConnector db) {
        this.db = db;
    }

    /**
     * Создание квеста
     * @return questId (из БД)
     */
    public int createQuest(Player player) throws SQLException {

        Quest quest = QuestGenerator.generateQuest();

        int playerId = db.getPlayerId(player.getUniqueId().toString());

        if (playerId == -1) {
            db.savePlayer(player.getUniqueId().toString(), player.getName());
            playerId = db.getPlayerId(player.getUniqueId().toString());
        }

        return db.saveQuest(
                playerId,
                quest.getType(),
                quest.getTarget(),
                quest.getAmount(),
                quest.getReward()
        );
    }

    public void completeQuest(int questId) throws SQLException {
        db.completeQuest(questId);
    }
    public void failQuest(int questId) throws SQLException {
        db.failQuest(questId);
    }
}