package com.example.questai.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

class QuestTest {

    private Quest quest;

    @BeforeEach
    void setUp() {
        quest = new Quest();
    }

    @Test
    void testQuestCreation() {
        assertNotNull(quest);
        assertNull(quest.getType());
        assertNull(quest.getTarget());
        assertEquals(0, quest.getAmount());
        assertEquals(0, quest.getReward());
    }

    @Test
    void testSetAndGetId() {
        quest.setId(100);
        assertEquals(100, quest.getId());
    }

    @Test
    void testSetAndGetType() {
        quest.setType("Kill");
        assertEquals("Kill", quest.getType());
        
        quest.setType("Break");
        assertEquals("Break", quest.getType());
        
        quest.setType("Collect");
        assertEquals("Collect", quest.getType());
    }

    @Test
    void testSetAndGetTarget() {
        quest.setTarget("ZOMBIE");
        assertEquals("ZOMBIE", quest.getTarget());
        
        quest.setTarget("STONE");
        assertEquals("STONE", quest.getTarget());
        
        quest.setTarget("DIAMOND");
        assertEquals("DIAMOND", quest.getTarget());
    }

    @Test
    void testSetAndGetAmount() {
        quest.setAmount(5);
        assertEquals(5, quest.getAmount());
        
        quest.setAmount(10);
        assertEquals(10, quest.getAmount());
        
        quest.setAmount(0);
        assertEquals(0, quest.getAmount());
    }

    @Test
    void testSetAndGetReward() {
        quest.setReward(50);
        assertEquals(50, quest.getReward());
        
        quest.setReward(100);
        assertEquals(100, quest.getReward());
        
        quest.setReward(0);
        assertEquals(0, quest.getReward());
    }

    @Test
    void testSetAndGetBiome() {
        quest.setBiome("plains");
        assertEquals("plains", quest.getBiome());
        
        quest.setBiome("desert");
        assertEquals("desert", quest.getBiome());
        
        quest.setBiome("nether");
        assertEquals("nether", quest.getBiome());
    }

    @Test
    void testAllFieldsSet() {
        quest.setId(1);
        quest.setType("Kill");
        quest.setTarget("SPIDER");
        quest.setAmount(3);
        quest.setReward(60);
        quest.setBiome("forest");
        
        assertEquals(1, quest.getId());
        assertEquals("Kill", quest.getType());
        assertEquals("SPIDER", quest.getTarget());
        assertEquals(3, quest.getAmount());
        assertEquals(60, quest.getReward());
        assertEquals("forest", quest.getBiome());
    }

    @Test
    void testToString() {
        quest.setId(1);
        quest.setType("Kill");
        quest.setTarget("ZOMBIE");
        quest.setAmount(5);
        quest.setReward(50);
        quest.setBiome("plains");
        
        String str = quest.toString();
        
        assertTrue(str.contains("id=1"));
        assertTrue(str.contains("type='Kill'"));
        assertTrue(str.contains("target='ZOMBIE'"));
        assertTrue(str.contains("amount=5"));
        assertTrue(str.contains("reward=50"));
        assertTrue(str.contains("biome='plains'"));
    }

    @Test
    void testDefaultValues() {
        assertEquals(0, quest.getId());
        assertNull(quest.getType());
        assertNull(quest.getTarget());
        assertEquals(0, quest.getAmount());
        assertEquals(0, quest.getReward());
        assertNull(quest.getBiome());
    }
}