package com.example.questai.db;

import java.sql.*;

public class DatabaseConnection {
    private static final String URL = "jdbc:postgresql://db:5432/quests";
    private static final String USER = "admin";
    private static final String PASS = "admin";
    
    private Connection conn;
    
    public DatabaseConnection() throws SQLException {
        connectWithRetry();
    }
    
    private void connectWithRetry() throws SQLException {
        int attempts = 15;
        
        while (attempts > 0) {
            try {
                System.out.println("[DatabaseConnection] Attempting connection to: " + URL);
                Class.forName("org.postgresql.Driver");
                conn = DriverManager.getConnection(URL, USER, PASS);
                System.out.println("[DatabaseConnection] Connected to PostgreSQL!");
                return;
            } catch (Exception e) {
                attempts--;
                System.err.println("[DatabaseConnection] Connection failed. Attempts left: " + attempts);
                if (attempts <= 0) {
                    throw new SQLException("Unable to connect to PostgreSQL", e);
                }
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ignored) {}
            }
        }
    }
    
    public Connection getConnection() {
        return conn;
    }
    
    public void close() {
        try {
            if (conn != null && !conn.isClosed()) {
                conn.close();
                System.out.println("[DatabaseConnection] Connection closed");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}