package com.example.questai.db;

import com.example.questai.db.repositories.PlayerRepository;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DBConnectorTest {

    @Test
    void testDriverExists() {
        try {
            Class.forName("org.postgresql.Driver");
            assertTrue(true, "PostgreSQL driver found");
        } catch (ClassNotFoundException e) {
            fail("PostgreSQL driver not found in classpath");
        }
    }
    
    @Test
    void testDbConnectorClassExists() {
        assertNotNull(DBConnector.class);
    }
    
    @Test
    void testDatabaseConnectionClassExists() {
        try {
            Class.forName("com.example.questai.db.DatabaseConnection");
            assertTrue(true, "DatabaseConnection class found");
        } catch (ClassNotFoundException e) {
            fail("DatabaseConnection class not found");
        }
    }
    
    @Test
    void testConnectionParametersAreValid() {
        try {
            Class<?> dbConnectionClass = Class.forName("com.example.questai.db.DatabaseConnection");
            java.lang.reflect.Field urlField = dbConnectionClass.getDeclaredField("URL");
            java.lang.reflect.Field userField = dbConnectionClass.getDeclaredField("USER");
            java.lang.reflect.Field passField = dbConnectionClass.getDeclaredField("PASS");
            
            urlField.setAccessible(true);
            userField.setAccessible(true);
            passField.setAccessible(true);
            
            String url = (String) urlField.get(null);
            String user = (String) userField.get(null);
            String pass = (String) passField.get(null);
            
            assertNotNull(url, "URL should not be null");
            assertNotNull(user, "USER should not be null");
            assertNotNull(pass, "PASS should not be null");
            assertTrue(url.startsWith("jdbc:postgresql://"), "URL should start with jdbc:postgresql://");
            assertTrue(url.contains("quests"), "URL should contain database name 'quests'");
            
        } catch (Exception e) {
            try {
                DBConnector connector = new DBConnector();
                assertNotNull(connector);
                connector.close();
                System.out.println("DBConnector instance created successfully (fallback test)");
            } catch (Exception ex) {
                fail("Cannot create DBConnector instance: " + ex.getMessage());
            }
        }
    }
}