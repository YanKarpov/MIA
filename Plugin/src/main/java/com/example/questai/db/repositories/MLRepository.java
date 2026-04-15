package com.example.questai.db.repositories;

import com.example.questai.model.Quest;
import com.example.questai.db.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MLRepository {
    private final DatabaseConnection db;
    
    public MLRepository(DatabaseConnection db) {
        this.db = db;
    }
    
    public void saveMlPrediction(int questId, int candidateIndex, double predictedScore, boolean wasSelected) throws SQLException {
        String sql = """
            INSERT INTO ml_predictions (quest_id, candidate_index, predicted_score, was_selected)
            VALUES (?, ?, ?, ?)
            """;
        
        try (PreparedStatement stmt = db.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, questId);
            stmt.setInt(2, candidateIndex);
            stmt.setDouble(3, predictedScore);
            stmt.setBoolean(4, wasSelected);
            stmt.executeUpdate();
        }
    }
    
    public List<Quest> getQuestsForTraining(int playerId, int limit) throws SQLException {
        String sql = """
            SELECT type, amount, deaths_before, kills_before, success_rate_before,
                   CASE WHEN status = 'COMPLETED' THEN 1 ELSE 0 END as completed
            FROM quests 
            WHERE player_id = ? AND status != 'IN_PROGRESS'
            ORDER BY issued_at DESC
            LIMIT ?
            """;
        
        List<Quest> quests = new ArrayList<>();
        try (PreparedStatement stmt = db.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, playerId);
            stmt.setInt(2, limit);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Quest q = new Quest();
                q.setType(rs.getString("type"));
                q.setAmount(rs.getInt("amount"));
                quests.add(q);
            }
        }
        return quests;
    }
}