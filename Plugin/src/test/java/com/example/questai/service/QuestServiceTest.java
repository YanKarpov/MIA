package com.example.questai.service;

import com.example.questai.model.Quest;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class QuestServiceTest {

    @Test
    void testQuestHasRequiredFields() {
        Quest quest = new Quest();
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
            Quest quest = new Quest();
            quest.setType(type);
            assertEquals(type, quest.getType());
        }
    }
    
    @Test
    void testRewardCalculation() {
        Quest quest = new Quest();
        quest.setType("Kill");
        quest.setAmount(10);
        quest.setReward(quest.getAmount() * 7); 
        
        assertEquals(70, quest.getReward());
    }
}