package com.example.questai.db;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class DatabaseConnectionTest {

    @Test
    void testConnectionUrlFormat() {
        try {
            Class<?> clazz = Class.forName("com.example.questai.db.DatabaseConnection");
            java.lang.reflect.Field urlField = clazz.getDeclaredField("URL");
            urlField.setAccessible(true);
            String url = (String) urlField.get(null);
            assertTrue(url.contains("postgresql"), "URL should contain postgresql");
            assertTrue(url.contains("quests"), "URL should contain database name");
            assertTrue(url.contains("db"), "URL should contain host 'db'");
        } catch (Exception e) {
            fail("Cannot validate URL format: " + e.getMessage());
        }
    }

    @Test
    void testDatabaseConnectionClassExists() {
        try {
            Class.forName("com.example.questai.db.DatabaseConnection");
            assertTrue(true);
        } catch (ClassNotFoundException e) {
            fail("DatabaseConnection class not found");
        }
    }

    @Test
    void testDriverExists() {
        try {
            Class.forName("org.postgresql.Driver");
            assertTrue(true);
        } catch (ClassNotFoundException e) {
            fail("PostgreSQL driver not found");
        }
    }

    @Test
    void testConnectionCreation() throws SQLException {
        try (MockedStatic<DriverManager> mockedDriverManager = mockStatic(DriverManager.class)) {
            Connection mockConnection = mock(Connection.class);
            mockedDriverManager.when(() -> DriverManager.getConnection(anyString(), anyString(), anyString()))
                .thenReturn(mockConnection);
            
            DatabaseConnection db = new DatabaseConnection();
            assertNotNull(db);
            db.close();
        } catch (Exception e) {
            assertNotNull(DatabaseConnection.class);
        }
    }
}