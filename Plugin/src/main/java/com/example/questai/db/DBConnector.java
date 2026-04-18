package com.example.questai.db;

import com.example.questai.db.repositories.*;
import com.example.questai.model.Player;
import com.example.questai.model.Quest;
import java.sql.SQLException;
import java.util.List;

public class DBConnector {
    private final DatabaseConnection dbConnection;
    private final PlayerRepository playerRepository;
    private final QuestRepository questRepository;
    private final PreferencesRepository preferencesRepository;
    private final MLRepository mlRepository;
    
    public DBConnector() throws SQLException {
        this.dbConnection = new DatabaseConnection();
        this.playerRepository = new PlayerRepository(dbConnection);
        this.questRepository = new QuestRepository(dbConnection, playerRepository);
        this.preferencesRepository = new PreferencesRepository(dbConnection);
        this.mlRepository = new MLRepository(dbConnection);
        
        createTablesIfNotExist();
        upgradeTablesIfNeeded();
    }
    
    private void createTablesIfNotExist() throws SQLException {
        String createPlayers = """
            CREATE TABLE IF NOT EXISTS players (
                id SERIAL PRIMARY KEY,
                uuid VARCHAR(36) UNIQUE NOT NULL,
                name VARCHAR(50),
                total_deaths INT DEFAULT 0,
                total_kills INT DEFAULT 0,
                total_quests INT DEFAULT 0,
                completed_quests INT DEFAULT 0,
                success_rate FLOAT DEFAULT 0.0,
                first_seen TIMESTAMP DEFAULT NOW(),
                last_seen TIMESTAMP DEFAULT NOW()
            );
            """;
        
        String createQuests = """
            CREATE TABLE IF NOT EXISTS quests (
                id SERIAL PRIMARY KEY,
                player_id INT REFERENCES players(id) ON DELETE CASCADE,
                type VARCHAR(20) NOT NULL,
                target VARCHAR(50) DEFAULT 'ANY',
                amount INT NOT NULL,
                reward INT NOT NULL,
                status VARCHAR(20) DEFAULT 'IN_PROGRESS',
                deaths_before INT NOT NULL,
                kills_before INT NOT NULL,
                success_rate_before FLOAT NOT NULL,
                deaths_after INT,
                kills_after INT,
                ml_score FLOAT,
                ml_selected BOOLEAN DEFAULT TRUE,
                issued_at TIMESTAMP DEFAULT NOW(),
                completed_at TIMESTAMP
            );
            """;
        
        String createMlPredictions = """
            CREATE TABLE IF NOT EXISTS ml_predictions (
                id SERIAL PRIMARY KEY,
                quest_id INT REFERENCES quests(id) ON DELETE CASCADE,
                candidate_index INT,
                predicted_score FLOAT,
                was_selected BOOLEAN DEFAULT FALSE,
                type VARCHAR(20),
                target VARCHAR(50),
                amount INT,
                reward INT,
                created_at TIMESTAMP DEFAULT NOW()
            );
            """;
        
        try (var stmt = dbConnection.getConnection().createStatement()) {
            stmt.execute(createPlayers);
            stmt.execute(createQuests);
            stmt.execute(createMlPredictions);
            System.out.println("[DBConnector] Tables created/verified");
        }
    }
    
    private void upgradeTablesIfNeeded() throws SQLException {
        String[] alterStatements = {
            "ALTER TABLE players ADD COLUMN IF NOT EXISTS total_deaths INT DEFAULT 0",
            "ALTER TABLE players ADD COLUMN IF NOT EXISTS total_kills INT DEFAULT 0",
            "ALTER TABLE players ADD COLUMN IF NOT EXISTS total_quests INT DEFAULT 0",
            "ALTER TABLE players ADD COLUMN IF NOT EXISTS completed_quests INT DEFAULT 0",
            "ALTER TABLE players ADD COLUMN IF NOT EXISTS success_rate FLOAT DEFAULT 0.0",
            "ALTER TABLE players ADD COLUMN IF NOT EXISTS first_seen TIMESTAMP DEFAULT NOW()",
            "ALTER TABLE players ADD COLUMN IF NOT EXISTS last_seen TIMESTAMP DEFAULT NOW()",
            "ALTER TABLE quests ADD COLUMN IF NOT EXISTS deaths_before INT",
            "ALTER TABLE quests ADD COLUMN IF NOT EXISTS kills_before INT",
            "ALTER TABLE quests ADD COLUMN IF NOT EXISTS success_rate_before FLOAT",
            "ALTER TABLE quests ADD COLUMN IF NOT EXISTS deaths_after INT",
            "ALTER TABLE quests ADD COLUMN IF NOT EXISTS kills_after INT",
            "ALTER TABLE quests ADD COLUMN IF NOT EXISTS ml_score FLOAT",
            "ALTER TABLE quests ADD COLUMN IF NOT EXISTS ml_selected BOOLEAN DEFAULT TRUE",
            "ALTER TABLE quests ADD COLUMN IF NOT EXISTS issued_at TIMESTAMP DEFAULT NOW()",
            "ALTER TABLE quests ADD COLUMN IF NOT EXISTS completed_at TIMESTAMP",
            "ALTER TABLE ml_predictions ADD COLUMN IF NOT EXISTS type VARCHAR(20)",
            "ALTER TABLE ml_predictions ADD COLUMN IF NOT EXISTS target VARCHAR(50)",
            "ALTER TABLE ml_predictions ADD COLUMN IF NOT EXISTS amount INT",
            "ALTER TABLE ml_predictions ADD COLUMN IF NOT EXISTS reward INT"
        };
        
        for (String sql : alterStatements) {
            try (var stmt = dbConnection.getConnection().createStatement()) {
                stmt.executeUpdate(sql);
            } catch (SQLException e) {
                if (!e.getMessage().contains("already exists")) {
                    System.err.println("Warning: " + e.getMessage());
                }
            }
        }
    }
    
    // Делегируем методы репозиториям
    
    public void savePlayer(String uuid, String name) throws SQLException {
        playerRepository.savePlayer(uuid, name);
    }
    
    public int getPlayerId(String uuid) throws SQLException {
        return playerRepository.getPlayerId(uuid);
    }
    
    public Player getPlayerStats(int playerId) throws SQLException {
        return playerRepository.getPlayerStats(playerId);
    }
    
    public void updatePlayerStats(int playerId) throws SQLException {
        playerRepository.updatePlayerStats(playerId);
    }
    
    public double getSuccessRate(int playerId) throws SQLException {
        return playerRepository.getSuccessRate(playerId);
    }
    
    public int getTotalQuestsCount(int playerId) throws SQLException {
        return playerRepository.getTotalQuestsCount(playerId);
    }
    
    public int getCompletedQuestsCount(int playerId) throws SQLException {
        return playerRepository.getCompletedQuestsCount(playerId);
    }
    
    public int saveQuest(int playerId, Quest quest, Player player, double mlScore, boolean mlSelected) throws SQLException {
        return questRepository.saveQuest(playerId, quest, player, mlScore, mlSelected);
    }
    
    public void completeQuest(int questId, int deathsAfter, int killsAfter) throws SQLException {
        questRepository.completeQuest(questId, deathsAfter, killsAfter);
    }
    
    public void failQuest(int questId, int deathsAfter, int killsAfter) throws SQLException {
        questRepository.failQuest(questId, deathsAfter, killsAfter);
    }
    
    public int getPlayerIdByQuestId(int questId) throws SQLException {
        return questRepository.getPlayerIdByQuestId(questId);
    }
    
    public String getQuestType(int questId) throws SQLException {
        return questRepository.getQuestType(questId);
    }
    
    public Quest getQuestById(int questId) throws SQLException {
        return questRepository.getQuestById(questId);
    }
    
    public String getLastQuestType(int playerId) throws SQLException {
        return preferencesRepository.getLastQuestType(playerId);
    }
    
    public int getConsecutiveSuccesses(int playerId) throws SQLException {
        return preferencesRepository.getConsecutiveSuccesses(playerId);
    }
    
    public String getFavoriteQuestType(int playerId) throws SQLException {
        return preferencesRepository.getFavoriteQuestType(playerId);
    }
    
    public String getLeastFavoriteQuestType(int playerId) throws SQLException {
        return preferencesRepository.getLeastFavoriteQuestType(playerId);
    }
    
    public String getPreferredTarget(int playerId) throws SQLException {
        return preferencesRepository.getPreferredTarget(playerId);
    }
    
    public void updateLastQuestType(int playerId, String questType) throws SQLException {
        preferencesRepository.updateLastQuestType(playerId, questType);
    }
    
    // Обновлённый метод с передачей объекта Quest
    public void saveMlPrediction(int questId, int candidateIndex, double predictedScore, 
                                  boolean wasSelected, Quest candidate) throws SQLException {
        mlRepository.saveMlPrediction(questId, candidateIndex, predictedScore, wasSelected, candidate);
    }
    
    // Сохранение всех предсказаний
    public void saveAllMlPredictions(int questId, List<Quest> candidates, 
                                      double[] scores, int bestIndex) throws SQLException {
        mlRepository.saveAllPredictions(questId, candidates, scores, bestIndex);
    }
    
    // Получение последних предсказаний для дашборда
    public List<MLRepository.MlPredictionWithQuest> getLatestPredictions(int limit) throws SQLException {
        return mlRepository.getLatestPredictions(limit);
    }
    
    // Метод для обучения модели
    public List<Quest> getQuestsForTraining(int playerId, int limit) throws SQLException {
        return mlRepository.getQuestsForTraining(playerId, limit);
    }
    
    public void close() {
        dbConnection.close();
    }
    
    public void updatePlayerQuestHistory(int questId, boolean completed) throws SQLException {
        System.out.println("[DBConnector] Quest history updated: questId=" + questId + ", completed=" + completed);
    }
}