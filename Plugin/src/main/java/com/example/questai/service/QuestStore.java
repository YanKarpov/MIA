package com.example.questai.service;

import com.example.questai.db.DBConnector;
import com.example.questai.model.Player;
import com.example.questai.model.Quest;
import com.example.questai.model.QuestCandidate;
import com.example.questai.ml.RankResponse;

import java.sql.SQLException;
import java.util.List;

public class QuestStore {
    private final DBConnector db;
    
    public QuestStore(DBConnector db) {
        this.db = db;
    }
    
    public int saveSelectedQuest(int playerId, Quest bestQuest, Player gamePlayer, double bestScore) throws SQLException {
        return db.saveQuest(playerId, bestQuest, gamePlayer, bestScore, true);
    }

    public void saveAllPredictions(int questId, List<QuestCandidate> candidates, 
                                    List<RankResponse> results, int bestIndex) throws SQLException {
        if (results == null || results.isEmpty()) return;
        
        for (RankResponse r : results) {
            QuestCandidate candidate = candidates.get(r.getQuestIndex());
            Quest quest = candidate.getQuest();
            db.saveMlPrediction(questId, r.getQuestIndex(), r.getScore(), 
                                r.getQuestIndex() == bestIndex, quest);
        }
    }
    
    public void updateLastQuestType(int playerId, String questType) throws SQLException {
        db.updateLastQuestType(playerId, questType);
    }
    
    public String getQuestType(int questId) throws SQLException {
        return db.getQuestType(questId);
    }
    
    public void completeQuest(int questId, int deathsAfter, int killsAfter) throws SQLException {
        db.completeQuest(questId, deathsAfter, killsAfter);
    }
    
    public void failQuest(int questId, int deathsAfter, int killsAfter) throws SQLException {
        db.failQuest(questId, deathsAfter, killsAfter);
    }
    
    public void updatePlayerQuestHistory(int questId, boolean completed) throws SQLException {
        db.updatePlayerQuestHistory(questId, completed);
    }
    
    public void updatePlayerStats(int playerId) throws SQLException {
        db.updatePlayerStats(playerId);
    }
    
    public double getSuccessRate(int playerId) throws SQLException {
        return db.getSuccessRate(playerId);
    }
    
    public int getPlayerIdByQuestId(int questId) throws SQLException {
        return db.getPlayerIdByQuestId(questId);
    }
    
    public Quest getQuestById(int questId) throws SQLException {
        return db.getQuestById(questId);
    }
}