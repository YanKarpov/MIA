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
    
    public void saveMlPrediction(int questId, int candidateIndex, double predictedScore, 
                                  boolean wasSelected, Quest candidate) throws SQLException {
        String sql = """
            INSERT INTO ml_predictions (
                quest_id, candidate_index, predicted_score, was_selected,
                type, target, amount, reward
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;
        
        try (PreparedStatement stmt = db.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, questId);
            stmt.setInt(2, candidateIndex);
            stmt.setDouble(3, predictedScore);
            stmt.setBoolean(4, wasSelected);
            stmt.setString(5, candidate.getType());
            stmt.setString(6, candidate.getTarget());
            stmt.setInt(7, candidate.getAmount());
            stmt.setInt(8, candidate.getReward());
            stmt.executeUpdate();
        }
    }
    
    public void saveAllPredictions(int questId, List<Quest> candidates, 
                                    double[] scores, int bestIndex) throws SQLException {
        for (int i = 0; i < candidates.size(); i++) {
            saveMlPrediction(questId, i, scores[i], i == bestIndex, candidates.get(i));
        }
    }
    
    // Получить последние предсказания с деталями кандидатов
    public List<MlPredictionWithQuest> getLatestPredictions(int limit) throws SQLException {
        String sql = """
            SELECT 
                candidate_index,
                predicted_score,
                was_selected,
                type,
                target,
                amount,
                reward,
                created_at
            FROM ml_predictions 
            WHERE quest_id = (
                SELECT quest_id FROM ml_predictions 
                ORDER BY created_at DESC LIMIT 1
            )
            ORDER BY candidate_index
            LIMIT ?
            """;
        
        List<MlPredictionWithQuest> predictions = new ArrayList<>();
        try (PreparedStatement stmt = db.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, limit);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                predictions.add(new MlPredictionWithQuest(
                    rs.getInt("candidate_index"),
                    rs.getDouble("predicted_score"),
                    rs.getBoolean("was_selected"),
                    rs.getString("type"),
                    rs.getString("target"),
                    rs.getInt("amount"),
                    rs.getInt("reward"),
                    rs.getTimestamp("created_at")
                ));
            }
        }
        return predictions;
    }
    
    // Метод для получения данных для обучения модели
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
    
    public static class MlPredictionWithQuest {
        public final int candidateIndex;
        public final double predictedScore;
        public final boolean wasSelected;
        public final String type;
        public final String target;
        public final int amount;
        public final int reward;
        public final Timestamp createdAt;
        
        public MlPredictionWithQuest(int candidateIndex, double predictedScore, boolean wasSelected,
                                      String type, String target, int amount, int reward, Timestamp createdAt) {
            this.candidateIndex = candidateIndex;
            this.predictedScore = predictedScore;
            this.wasSelected = wasSelected;
            this.type = type;
            this.target = target;
            this.amount = amount;
            this.reward = reward;
            this.createdAt = createdAt;
        }
    }
}