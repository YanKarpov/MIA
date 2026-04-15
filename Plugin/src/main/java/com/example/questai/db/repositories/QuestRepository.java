package com.example.questai.db.repositories;

import com.example.questai.model.Player;
import com.example.questai.model.Quest;
import com.example.questai.db.DatabaseConnection;
import java.sql.*;

public class QuestRepository {
    private final DatabaseConnection db;
    private final PlayerRepository playerRepository;
    
    public QuestRepository(DatabaseConnection db, PlayerRepository playerRepository) {
        this.db = db;
        this.playerRepository = playerRepository;
    }
    
    public int saveQuest(int playerId, Quest quest, Player player, double mlScore, boolean mlSelected) throws SQLException {
        String sql = """
            INSERT INTO quests (
                player_id, type, target, amount, reward, status,
                deaths_before, kills_before, success_rate_before,
                ml_score, ml_selected
            ) VALUES (?, ?, ?, ?, ?, 'IN_PROGRESS', ?, ?, ?, ?, ?)
            RETURNING id
            """;
        
        try (PreparedStatement stmt = db.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, playerId);
            stmt.setString(2, quest.getType());
            stmt.setString(3, quest.getTarget());
            stmt.setInt(4, quest.getAmount());
            stmt.setInt(5, quest.getReward());
            stmt.setInt(6, player.getDeaths());
            stmt.setInt(7, player.getKills());
            stmt.setDouble(8, player.getSuccessRate());
            stmt.setDouble(9, mlScore);
            stmt.setBoolean(10, mlSelected);
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int id = rs.getInt("id");
                System.out.println("[QuestRepository] Quest saved ID: " + id);
                return id;
            }
        }
        return -1;
    }
    
    public void completeQuest(int questId, int deathsAfter, int killsAfter) throws SQLException {
        String sql = """
            UPDATE quests
            SET status = 'COMPLETED',
                deaths_after = ?,
                kills_after = ?,
                completed_at = NOW()
            WHERE id = ?
            """;
        
        try (PreparedStatement stmt = db.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, deathsAfter);
            stmt.setInt(2, killsAfter);
            stmt.setInt(3, questId);
            stmt.executeUpdate();
        }
        
        int playerId = getPlayerIdByQuestId(questId);
        if (playerId != -1) {
            playerRepository.updatePlayerStats(playerId);
        }
    }
    
    public void failQuest(int questId, int deathsAfter, int killsAfter) throws SQLException {
        String sql = """
            UPDATE quests
            SET status = 'FAILED',
                deaths_after = ?,
                kills_after = ?,
                completed_at = NOW()
            WHERE id = ?
            """;
        
        try (PreparedStatement stmt = db.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, deathsAfter);
            stmt.setInt(2, killsAfter);
            stmt.setInt(3, questId);
            stmt.executeUpdate();
        }
        
        int playerId = getPlayerIdByQuestId(questId);
        if (playerId != -1) {
            playerRepository.updatePlayerStats(playerId);
        }
    }
    
    public int getPlayerIdByQuestId(int questId) throws SQLException {
        String sql = "SELECT player_id FROM quests WHERE id = ?";
        try (PreparedStatement stmt = db.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, questId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("player_id");
            }
        }
        return -1;
    }
    
    public String getQuestType(int questId) throws SQLException {
        String sql = "SELECT type FROM quests WHERE id = ?";
        try (PreparedStatement stmt = db.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, questId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("type");
            }
        }
        return null;
    }
    
    public Quest getQuestById(int questId) throws SQLException {
        String sql = "SELECT type, target, amount, reward FROM quests WHERE id = ?";
        
        try (PreparedStatement stmt = db.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, questId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                Quest quest = new Quest();
                quest.setId(questId);
                quest.setType(rs.getString("type"));
                quest.setTarget(rs.getString("target"));
                quest.setAmount(rs.getInt("amount"));
                quest.setReward(rs.getInt("reward"));
                return quest;
            }
        }
        return null;
    }
}