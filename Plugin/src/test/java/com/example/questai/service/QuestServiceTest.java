package com.example.questai.service;

import com.example.questai.model.Quest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

class QuestServiceTest {

    private Quest quest;

    @BeforeEach
    void setUp() {
        quest = new Quest();
    }

    @Test
    void testQuestHasRequiredFields() {
        quest.setType("Kill");
        quest.setTarget("ZOMBIE");
        quest.setAmount(5);
        quest.setReward(25);
        
        assertNotNull(quest.getType());
        assertNotNull(quest.getTarget());
        assertTrue(quest.getAmount() > 0);
        assertTrue(quest.getReward() > 0);
    }
    
    @Test
    void testQuestTypesAreValid() {
        String[] validTypes = {"Kill", "Collect", "Break"};
        
        for (String type : validTypes) {
            quest.setType(type);
            assertEquals(type, quest.getType());
        }
    }
    
    @Test
    void testRewardCalculation() {
        quest.setType("Kill");
        quest.setAmount(10);
        quest.setReward(quest.getAmount() * 7);
        
        assertEquals(70, quest.getReward());
    }

    @Test
    void testQuestRewardBasedOnType() {
        quest.setType("Kill");
        quest.setAmount(5);
        quest.setReward(quest.getAmount() * 8);
        assertEquals(40, quest.getReward());
        
        quest.setType("Collect");
        quest.setReward(quest.getAmount() * 5);
        assertEquals(25, quest.getReward());
        
        quest.setType("Break");
        quest.setReward(quest.getAmount() * 6);
        assertEquals(30, quest.getReward());
    }

    @Test
    void testQuestAmountRange() {
        for (int amount = 1; amount <= 30; amount++) {
            quest.setAmount(amount);
            assertTrue(quest.getAmount() >= 1, "Amount should be >= 1");
            assertTrue(quest.getAmount() <= 30, "Amount should be <= 30");
        }
    }

    @Test
    void testQuestRewardPositive() {
        quest.setReward(0);
        assertTrue(quest.getReward() >= 0);
        
        quest.setReward(100);
        assertEquals(100, quest.getReward());
    }

    @Test
    void testQuestTargetNotEmpty() {
        quest.setTarget("ZOMBIE");
        assertNotNull(quest.getTarget());
        assertFalse(quest.getTarget().isEmpty());
        
        quest.setTarget("DIAMOND");
        assertEquals("DIAMOND", quest.getTarget());
    }

    @Test
    void testQuestBiomeOptional() {
        assertNull(quest.getBiome());
        
        quest.setBiome("plains");
        assertEquals("plains", quest.getBiome());
        
        quest.setBiome("desert");
        assertEquals("desert", quest.getBiome());
    }

    @Test
    void testQuestIdAssignment() {
        quest.setId(1);
        assertEquals(1, quest.getId());
        
        quest.setId(999);
        assertEquals(999, quest.getId());
    }

    @Test
    void testCompleteQuestObject() {
        quest.setId(100);
        quest.setType("Kill");
        quest.setTarget("ENDER_DRAGON");
        quest.setAmount(1);
        quest.setReward(500);
        quest.setBiome("end");
        
        assertAll("quest",
            () -> assertEquals(100, quest.getId()),
            () -> assertEquals("Kill", quest.getType()),
            () -> assertEquals("ENDER_DRAGON", quest.getTarget()),
            () -> assertEquals(1, quest.getAmount()),
            () -> assertEquals(500, quest.getReward()),
            () -> assertEquals("end", quest.getBiome())
        );
    }

    @Test
    void testQuestServiceClassExists() {
        assertNotNull(QuestService.class);
    }
}