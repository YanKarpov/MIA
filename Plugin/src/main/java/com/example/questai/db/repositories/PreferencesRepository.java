package com.example.questai.db.repositories;

import com.example.questai.db.DatabaseConnection;
import java.sql.*;

public class PreferencesRepository {
    private final DatabaseConnection db;
    
    public PreferencesRepository(DatabaseConnection db) {
        this.db = db;
    }
    
    public String getLastQuestType(int playerId) throws SQLException {
        String sql = """
            SELECT type FROM quests 
            WHERE player_id = ? 
            ORDER BY issued_at DESC 
            LIMIT 1
            """;
        
        try (PreparedStatement stmt = db.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, playerId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("type");
            }
        }
        return null;
    }
    
    public int getConsecutiveSuccesses(int playerId) throws SQLException {
        String sql = """
            WITH ordered_quests AS (
                SELECT status, 
                       ROW_NUMBER() OVER (ORDER BY issued_at DESC) as rn
                FROM quests 
                WHERE player_id = ? AND status != 'IN_PROGRESS'
                ORDER BY issued_at DESC
            )
            SELECT COUNT(*) FROM ordered_quests 
            WHERE rn < (
                SELECT COALESCE(MIN(rn), 1)
                FROM ordered_quests 
                WHERE status = 'FAILED'
            ) AND status = 'COMPLETED'
            """;
        
        try (PreparedStatement stmt = db.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, playerId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }
    
    public String getFavoriteQuestType(int playerId) throws SQLException {
        String sql = """
            SELECT type, 
                   COUNT(*) FILTER (WHERE status = 'COMPLETED')::FLOAT / NULLIF(COUNT(*), 0) as success_rate
            FROM quests 
            WHERE player_id = ? AND status != 'IN_PROGRESS'
            GROUP BY type
            ORDER BY success_rate DESC, COUNT(*) DESC
            LIMIT 1
            """;
        
        try (PreparedStatement stmt = db.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, playerId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("type");
            }
        }
        return null;
    }
    
    public String getLeastFavoriteQuestType(int playerId) throws SQLException {
        String sql = """
            SELECT type, 
                   COUNT(*) FILTER (WHERE status = 'COMPLETED')::FLOAT / NULLIF(COUNT(*), 0) as success_rate
            FROM quests 
            WHERE player_id = ? AND status != 'IN_PROGRESS'
            GROUP BY type
            ORDER BY success_rate ASC, COUNT(*) DESC
            LIMIT 1
            """;
        
        try (PreparedStatement stmt = db.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, playerId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("type");
            }
        }
        return null;
    }
    
    public String getPreferredTarget(int playerId) throws SQLException {
        String sql = """
            SELECT target, COUNT(*) as cnt
            FROM quests 
            WHERE player_id = ? AND status = 'COMPLETED'
            GROUP BY target
            ORDER BY cnt DESC
            LIMIT 1
            """;
        
        try (PreparedStatement stmt = db.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, playerId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("target");
            }
        }
        return null;
    }
    
    public void updateLastQuestType(int playerId, String questType) throws SQLException {
        String createTable = """
            CREATE TABLE IF NOT EXISTS player_preferences (
                player_id INT PRIMARY KEY REFERENCES players(id) ON DELETE CASCADE,
                last_quest_type VARCHAR(20),
                updated_at TIMESTAMP DEFAULT NOW()
            )
            """;
        
        try (Statement stmt = db.getConnection().createStatement()) {
            stmt.execute(createTable);
        } catch (SQLException e) {
            // Таблица возможно уже существует
        }
        
        String sql = """
            INSERT INTO player_preferences (player_id, last_quest_type, updated_at)
            VALUES (?, ?, NOW())
            ON CONFLICT (player_id) DO UPDATE SET
                last_quest_type = EXCLUDED.last_quest_type,
                updated_at = NOW()
            """;
        
        try (PreparedStatement stmt = db.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, playerId);
            stmt.setString(2, questType);
            stmt.executeUpdate();
            System.out.println("[PreferencesRepository] Updated last quest type for player " + playerId + ": " + questType);
        }
    }
}