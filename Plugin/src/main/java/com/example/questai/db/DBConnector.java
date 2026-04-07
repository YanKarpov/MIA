package com.example.questai.db;

import com.example.questai.model.Player;
import com.example.questai.model.Quest;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DBConnector {

    private static final String URL = "jdbc:postgresql://db:5432/quests";
    private static final String USER = "admin";
    private static final String PASS = "admin";

    private Connection conn;

    public DBConnector() throws SQLException {
        connectWithRetry();
        createTablesIfNotExist();
        upgradeTablesIfNeeded();
    }

    private void connectWithRetry() throws SQLException {
        int attempts = 15;

        while (attempts > 0) {
            try {
                System.out.println("[DBConnector] Attempting connection to: " + URL);
                Class.forName("org.postgresql.Driver");
                conn = DriverManager.getConnection(URL, USER, PASS);
                System.out.println("[DBConnector] Connected to PostgreSQL!");
                return;
            } catch (Exception e) {
                attempts--;
                System.err.println("[DBConnector] Connection failed. Attempts left: " + attempts);
                if (attempts <= 0) {
                    throw new SQLException("Unable to connect to PostgreSQL", e);
                }
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ignored) {}
            }
        }
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
                created_at TIMESTAMP DEFAULT NOW()
            );
            """;

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(createPlayers);
            stmt.execute(createQuests);
            stmt.execute(createMlPredictions);
            System.out.println("[DBConnector] Tables created/verified");
        }
    }

    private void upgradeTablesIfNeeded() throws SQLException {
        // Добавляем новые колонки, если их нет (для существующей БД)
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
            "ALTER TABLE quests ADD COLUMN IF NOT EXISTS completed_at TIMESTAMP"
        };
        
        for (String sql : alterStatements) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate(sql);
            } catch (SQLException e) {
                // Колонка возможно уже существует
                if (!e.getMessage().contains("already exists")) {
                    System.err.println("Warning: " + e.getMessage());
                }
            }
        }
    }

    // ==================== PLAYERS ====================

    public void savePlayer(String uuid, String name) throws SQLException {
        String sql = """
            INSERT INTO players (uuid, name, first_seen, last_seen) 
            VALUES (?, ?, NOW(), NOW()) 
            ON CONFLICT (uuid) DO UPDATE SET 
                name = EXCLUDED.name,
                last_seen = NOW()
            """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, uuid);
            stmt.setString(2, name);
            stmt.executeUpdate();
        }
    }

    public int getPlayerId(String uuid) throws SQLException {
        String sql = "SELECT id FROM players WHERE uuid = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
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
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
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
        String sql = """
            UPDATE players SET
                total_quests = (SELECT COUNT(*) FROM quests WHERE player_id = ?),
                completed_quests = (SELECT COUNT(*) FROM quests WHERE player_id = ? AND status = 'COMPLETED'),
                success_rate = COALESCE(
                    (SELECT COUNT(*)::FLOAT / NULLIF(COUNT(*), 0) 
                     FROM quests WHERE player_id = ? AND status = 'COMPLETED'), 0),
                last_seen = NOW()
            WHERE id = ?
            """;
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, playerId);
            stmt.setInt(2, playerId);
            stmt.setInt(3, playerId);
            stmt.setInt(4, playerId);
            stmt.executeUpdate();
        }
    }

    // ==================== QUESTS ====================

    public int saveQuest(int playerId, Quest quest, Player player, double mlScore, boolean mlSelected) throws SQLException {
        String sql = """
            INSERT INTO quests (
                player_id, type, target, amount, reward, status,
                deaths_before, kills_before, success_rate_before,
                ml_score, ml_selected
            ) VALUES (?, ?, ?, ?, ?, 'IN_PROGRESS', ?, ?, ?, ?, ?)
            RETURNING id
            """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
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
                System.out.println("[DBConnector] Quest saved ID: " + id);
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

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, deathsAfter);
            stmt.setInt(2, killsAfter);
            stmt.setInt(3, questId);
            stmt.executeUpdate();
        }
        
        // Получаем player_id и обновляем статистику игрока
        int playerId = getPlayerIdByQuestId(questId);
        if (playerId != -1) {
            updatePlayerStats(playerId);
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

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, deathsAfter);
            stmt.setInt(2, killsAfter);
            stmt.setInt(3, questId);
            stmt.executeUpdate();
        }
        
        // Обновляем статистику игрока
        int playerId = getPlayerIdByQuestId(questId);
        if (playerId != -1) {
            updatePlayerStats(playerId);
        }
    }

    private int getPlayerIdByQuestId(int questId) throws SQLException {
        String sql = "SELECT player_id FROM quests WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, questId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("player_id");
            }
        }
        return -1;
    }

    public double getSuccessRate(int playerId) throws SQLException {
        String sql = "SELECT success_rate FROM players WHERE id = ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, playerId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("success_rate");
            }
        }
        return 0.0;
    }

    // ==================== ML PREDICTIONS ====================

    public void saveMlPrediction(int questId, int candidateIndex, double predictedScore, boolean wasSelected) throws SQLException {
        String sql = """
            INSERT INTO ml_predictions (quest_id, candidate_index, predicted_score, was_selected)
            VALUES (?, ?, ?, ?)
            """;
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
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
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
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

    public void close() {
        try {
            if (conn != null && !conn.isClosed()) {
                conn.close();
                System.out.println("[DBConnector] Connection closed");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}