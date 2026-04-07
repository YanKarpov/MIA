package com.example.questai.db;

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
    void testConnectionParametersAreValid() {
        try {
            java.lang.reflect.Field urlField = DBConnector.class.getDeclaredField("URL");
            java.lang.reflect.Field userField = DBConnector.class.getDeclaredField("USER");
            java.lang.reflect.Field passField = DBConnector.class.getDeclaredField("PASS");
            
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
            
        } catch (NoSuchFieldException | IllegalAccessException e) {
            fail("Cannot access DBConnector fields: " + e.getMessage());
        }
    }
}