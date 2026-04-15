package com.example.questai.db.repositories;

import com.example.questai.model.Player;
import com.example.questai.db.DatabaseConnection;
import java.sql.*;

public class PlayerRepository {
    private final DatabaseConnection db;
    
    public PlayerRepository(DatabaseConnection db) {
        this.db = db;
    }
    
    public void savePlayer(String uuid, String name) throws SQLException {
        String sql = """
            INSERT INTO players (uuid, name, first_seen, last_seen) 
            VALUES (?, ?, NOW(), NOW()) 
            ON CONFLICT (uuid) DO UPDATE SET 
                name = EXCLUDED.name,
                last_seen = NOW()
            """;
        
        try (PreparedStatement stmt = db.getConnection().prepareStatement(sql)) {
            stmt.setString(1, uuid);
            stmt.setString(2, name);
            stmt.executeUpdate();
        }
    }
    
    public int getPlayerId(String uuid) throws SQLException {
        String sql = "SELECT id FROM players WHERE uuid = ?";
        
        try (PreparedStatement stmt = db.getConnection().prepareStatement(sql)) {
            stmt.setString(1, uuid);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
        }
        return -1;
    }
    
    public Player getPlayerStats(int playerId) throws SQLException {
        String sql = "SELECT * FROM players WHERE id = ?";
        
        try (PreparedStatement stmt = db.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, playerId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                Player player = new Player();
                player.setId(rs.getInt("id"));
                player.setUuid(rs.getString("uuid"));
                player.setName(rs.getString("name"));
                player.setDeaths(rs.getInt("total_deaths"));
                player.setKills(rs.getInt("total_kills"));
                player.setTotalQuests(rs.getInt("total_quests"));
                player.setCompletedQuests(rs.getInt("completed_quests"));
                player.setSuccessRate(rs.getDouble("success_rate"));
                return player;
            }
        }
        return null;
    }
    
    public void updatePlayerStats(int playerId) throws SQLException {
        String statsSql = """
            SELECT 
                COUNT(*) FILTER (WHERE status != 'IN_PROGRESS') as total_quests,
                COUNT(*) FILTER (WHERE status = 'COMPLETED') as completed_quests
            FROM quests 
            WHERE player_id = ?
            """;
        
        int totalQuests = 0;
        int completedQuests = 0;
        
        try (PreparedStatement stmt = db.getConnection().prepareStatement(statsSql)) {
            stmt.setInt(1, playerId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                totalQuests = rs.getInt("total_quests");
                completedQuests = rs.getInt("completed_quests");
            }
        }
        
        double successRate = (totalQuests == 0) ? 0.0 : (double) completedQuests / totalQuests;
        
        String updateSql = """
            UPDATE players 
            SET total_quests = ?, 
                completed_quests = ?, 
                success_rate = ?,
                last_seen = NOW()
            WHERE id = ?
            """;
        
        try (PreparedStatement stmt = db.getConnection().prepareStatement(updateSql)) {
            stmt.setInt(1, totalQuests);
            stmt.setInt(2, completedQuests);
            stmt.setDouble(3, successRate);
            stmt.setInt(4, playerId);
            stmt.executeUpdate();
        }
        
        System.out.println("[PlayerRepository] Updated player " + playerId + 
                        ": total=" + totalQuests + 
                        ", completed=" + completedQuests + 
                        ", success_rate=" + String.format("%.2f", successRate));
    }
    
    public double getSuccessRate(int playerId) throws SQLException {
        String sql = "SELECT success_rate FROM players WHERE id = ?";
        
        try (PreparedStatement stmt = db.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, playerId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("success_rate");
            }
        }
        return 0.0;
    }
    
    public int getTotalQuestsCount(int playerId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM quests WHERE player_id = ? AND status != 'IN_PROGRESS'";
        try (PreparedStatement stmt = db.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, playerId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }
    
    public int getCompletedQuestsCount(int playerId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM quests WHERE player_id = ? AND status = 'COMPLETED'";
        try (PreparedStatement stmt = db.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, playerId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }
}