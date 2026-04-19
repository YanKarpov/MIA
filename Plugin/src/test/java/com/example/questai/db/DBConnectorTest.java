package com.example.questai.db;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DBConnectorTest {

    @Test
    void testDbConnectorClassExists() {
        assertNotNull(DBConnector.class);
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
}