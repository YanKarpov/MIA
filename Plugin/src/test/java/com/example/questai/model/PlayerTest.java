package com.example.questai.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

class PlayerTest {

    private Player player;

    @BeforeEach
    void setUp() {
        player = new Player("test-uuid", "TestPlayer");
    }

    @Test
    void testPlayerCreation() {
        assertNotNull(player);
        assertEquals("test-uuid", player.getUuid());
        assertEquals("TestPlayer", player.getName());
        assertEquals(0, player.getDeaths());
        assertEquals(0, player.getKills());
        assertEquals(0.5, player.getSuccessRate(), 0.01);
    }

    @Test
    void testUpdateSuccessRate() {
        player.addCompletedQuest();
        player.addCompletedQuest();
        player.addFailedQuest();
        
        assertEquals(3, player.getTotalQuests());
        assertEquals(2, player.getCompletedQuests());
        assertEquals(2.0/3.0, player.getSuccessRate(), 0.01);
    }

    @Test
    void testSuccessRateWithNoQuests() {
        assertEquals(0.5, player.getSuccessRate(), 0.01);
    }

    @Test
    void testAddCompletedQuest() {
        player.addCompletedQuest();
        
        assertEquals(1, player.getTotalQuests());
        assertEquals(1, player.getCompletedQuests());
        assertEquals(1.0, player.getSuccessRate(), 0.01);
    }

    @Test
    void testAddFailedQuest() {
        player.addFailedQuest();
        
        assertEquals(1, player.getTotalQuests());
        assertEquals(0, player.getCompletedQuests());
        assertEquals(0.0, player.getSuccessRate(), 0.01);
    }

    @Test
    void testToDTO() {
        player.setDeaths(10);
        player.setKills(50);
        player.setSuccessRate(0.7);
        
        var dto = player.toDTO();
        
        assertEquals(10, dto.getDeaths());
        assertEquals(50, dto.getKills());
        assertEquals(0.7, dto.getSuccessRate(), 0.01);
    }

    @Test
    void testSettersAndGetters() {
        player.setId(1);
        player.setDeaths(20);
        player.setKills(100);
        player.setLastQuestType("Kill");
        player.setConsecutiveSuccesses(3);
        player.setFavoriteType("Kill");
        player.setLeastFavoriteType("Collect");
        player.setPreferredTarget("ZOMBIE");
        
        assertEquals(1, player.getId());
        assertEquals(20, player.getDeaths());
        assertEquals(100, player.getKills());
        assertEquals("Kill", player.getLastQuestType());
        assertEquals(3, player.getConsecutiveSuccesses());
        assertEquals("Kill", player.getFavoriteType());
        assertEquals("Collect", player.getLeastFavoriteType());
        assertEquals("ZOMBIE", player.getPreferredTarget());
    }

    @Test
    void testConstructorWithParams() {
        Player p = new Player(1, "uuid", "Name", 10, 20, 5, 3, 0.6);
        
        assertEquals(1, p.getId());
        assertEquals("uuid", p.getUuid());
        assertEquals("Name", p.getName());
        assertEquals(10, p.getDeaths());
        assertEquals(20, p.getKills());
        assertEquals(5, p.getTotalQuests());
        assertEquals(3, p.getCompletedQuests());
        assertEquals(0.6, p.getSuccessRate(), 0.01);
    }

    @Test
    void testToString() {
        player.setId(1);
        player.setName("TestPlayer");
        
        String str = player.toString();
        
        assertTrue(str.contains("TestPlayer"));
        assertTrue(str.contains("id=1"));
    }
}