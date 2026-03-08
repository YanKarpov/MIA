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
                System.out.println("[DBConnector] Username: " + USER);
                
                try {
                    Class.forName("org.postgresql.Driver");
                    System.out.println("[DBConnector] Driver loaded successfully");
                } catch (ClassNotFoundException e) {
                    System.err.println("[DBConnector] CRITICAL: PostgreSQL Driver not found!");
                    System.err.println("[DBConnector] Make sure postgresql.jar is included in the plugin");
                    e.printStackTrace();
                    throw new SQLException("PostgreSQL JDBC Driver not found", e);
                }
                
                conn = DriverManager.getConnection(URL, USER, PASS);
                System.out.println("[DBConnector] Connected to PostgreSQL successfully!");
                
                try (Statement stmt = conn.createStatement()) {
                    ResultSet rs = stmt.executeQuery("SELECT 1, current_database(), current_user, version()");
                    if (rs.next()) {
                        System.out.println("[DBConnector] Test query: " + rs.getInt(1));
                        System.out.println("[DBConnector] Database: " + rs.getString(2));
                        System.out.println("[DBConnector] User: " + rs.getString(3));
                        System.out.println("[DBConnector] Version: " + rs.getString(4));
                    }
                }
                
                return;
            } catch (SQLException e) {
                attempts--;
                System.err.println("[DBConnector] Connection failed! Attempts left: " + attempts);
                System.err.println("[DBConnector] SQL Error Message: " + e.getMessage());
                System.err.println("[DBConnector] SQL State: " + e.getSQLState());
                System.err.println("[DBConnector] Error Code: " + e.getErrorCode());
                System.err.println("[DBConnector] Stack trace:");
                e.printStackTrace();  
                
                if (e.getCause() != null) {
                    System.err.println("[DBConnector] Caused by: " + e.getCause().getMessage());
                    e.getCause().printStackTrace();
                }
                
                if (attempts > 0) {
                    System.out.println("[DBConnector] Waiting 2 seconds before retry...");
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException ignored) {}
                }
            }
        }
        throw new SQLException("Unable to connect to PostgreSQL after multiple attempts");
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
                reward VARCHAR(50),
                completed BOOLEAN DEFAULT FALSE,
                timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            );
            """;

        try (Statement stmt = conn.createStatement()) {
            System.out.println("[DBConnector] Creating players table if not exists...");
            stmt.execute(createPlayers);
            System.out.println("[DBConnector] Creating quests table if not exists...");
            stmt.execute(createQuests);
            System.out.println("[DBConnector] Tables ready");
        }
    }

    public void savePlayer(String uuid, String name) throws SQLException {
        System.out.println("[DBConnector] Saving player: " + name + " (" + uuid + ")");
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
            if (rs.next()) return rs.getInt("id");
        }
        return -1;
    }

    public void saveQuest(int playerId, String type, String target, int amount, String reward, boolean completed) throws SQLException {
        System.out.println("[DBConnector] Saving quest: " + type + " for player ID " + playerId);
        String sql = "INSERT INTO quests (player_id, type, target, amount, reward, completed) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, playerId);
            stmt.setString(2, type);
            stmt.setString(3, target);
            stmt.setInt(4, amount);
            stmt.setString(5, reward);
            stmt.setBoolean(6, completed);
            stmt.executeUpdate();
        }
    }

    public void close() {
        try {
            if (conn != null && !conn.isClosed()) conn.close();
            System.out.println("[DBConnector] Connection closed");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}