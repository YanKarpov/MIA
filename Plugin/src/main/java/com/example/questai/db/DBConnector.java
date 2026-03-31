package com.example.questai.db;

import java.sql.*;

public class DBConnector {

    private static final String URL = "jdbc:postgresql://db:5432/quests";
    private static final String USER = "admin";
    private static final String PASS = "admin";

    private Connection conn;

    public DBConnector() throws SQLException {
        connectWithRetry();
        createTablesIfNotExist();
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
                name VARCHAR(50)
            );
            """;

        String createQuests = """
            CREATE TABLE IF NOT EXISTS quests (
                id SERIAL PRIMARY KEY,
                player_id INT REFERENCES players(id),
                type VARCHAR(20),
                target VARCHAR(50),
                amount INT,
                reward INT,
                status VARCHAR(20),

                deaths INT,
                kills INT,
                success BOOLEAN,

                timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            );
            """;

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(createPlayers);
            stmt.execute(createQuests);
        }
    }

    public void savePlayer(String uuid, String name) throws SQLException {
        String sql = "INSERT INTO players (uuid, name) VALUES (?, ?) ON CONFLICT (uuid) DO NOTHING";

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

    public int saveQuest(int playerId, String type, String target, int amount, int reward) throws SQLException {

        String sql = """
            INSERT INTO quests (player_id, type, target, amount, reward, status)
            VALUES (?, ?, ?, ?, ?, ?)
            RETURNING id
        """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, playerId);
            stmt.setString(2, type);
            stmt.setString(3, target);
            stmt.setInt(4, amount);
            stmt.setInt(5, reward);
            stmt.setString(6, "IN_PROGRESS");

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int id = rs.getInt("id");
                System.out.println("[DBConnector] Quest saved ID: " + id);
                return id;
            }
        }

        return -1;
    }

    public void completeQuest(int questId, int deaths, int kills) throws SQLException {

        String sql = """
            UPDATE quests
            SET status = 'COMPLETED',
                deaths = ?,
                kills = ?,
                success = true
            WHERE id = ?
        """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, deaths);
            stmt.setInt(2, kills);
            stmt.setInt(3, questId);
            stmt.executeUpdate();
        }
    }

    public void failQuest(int questId, int deaths, int kills) throws SQLException {

        String sql = """
            UPDATE quests
            SET status = 'FAILED',
                deaths = ?,
                kills = ?,
                success = false
            WHERE id = ?
        """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, deaths);
            stmt.setInt(2, kills);
            stmt.setInt(3, questId);
            stmt.executeUpdate();
        }
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

    public double getSuccessRate(int playerId) throws SQLException {

        String sql = """
            SELECT 
                COUNT(*) FILTER (WHERE success = true) AS wins,
                COUNT(*) AS total
            FROM quests
            WHERE player_id = ?
        """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, playerId);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {

                int wins = rs.getInt("wins");
                int total = rs.getInt("total");

                if (total == 0) {
                    return 0.0; 
                }

                return (double) wins / total;
            }
        }

        return 0.0;
    }
}