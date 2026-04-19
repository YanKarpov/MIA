package com.example.questai.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

class QuestProgressTest {

    private Quest quest;
    private QuestProgress progress;

    @BeforeEach
    void setUp() {
        quest = new Quest();
        quest.setId(100);
        quest.setType("Kill");
        quest.setTarget("ZOMBIE");
        quest.setAmount(5);
        quest.setReward(50);
        
        progress = new QuestProgress(100, quest);
    }

    @Test
    void testQuestProgressCreation() {
        assertNotNull(progress);
        assertEquals(100, progress.getQuestId());
        assertNotNull(progress.getQuest());
        assertEquals(0, progress.getCurrent());
    }

    @Test
    void testGetQuestId() {
        assertEquals(100, progress.getQuestId());
    }

    @Test
    void testGetQuest() {
        Quest result = progress.getQuest();
        
        assertEquals(100, result.getId());
        assertEquals("Kill", result.getType());
        assertEquals("ZOMBIE", result.getTarget());
        assertEquals(5, result.getAmount());
        assertEquals(50, result.getReward());
    }

    @Test
    void testGetCurrentInitialValue() {
        assertEquals(0, progress.getCurrent());
    }

    @Test
    void testIncrementOnce() {
        progress.increment();
        assertEquals(1, progress.getCurrent());
    }

    @Test
    void testIncrementMultipleTimes() {
        progress.increment();
        progress.increment();
        progress.increment();
        
        assertEquals(3, progress.getCurrent());
    }

    @Test
    void testIncrementUpToAmount() {
        for (int i = 0; i < 5; i++) {
            progress.increment();
        }
        
        assertEquals(5, progress.getCurrent());
    }

    @Test
    void testIncrementBeyondAmount() {
        for (int i = 0; i < 10; i++) {
            progress.increment();
        }
        
        assertEquals(10, progress.getCurrent());
    }

    @Test
    void testQuestProgressWithDifferentQuest() {
        Quest anotherQuest = new Quest();
        anotherQuest.setId(200);
        anotherQuest.setType("Collect");
        anotherQuest.setTarget("DIAMOND");
        anotherQuest.setAmount(3);
        anotherQuest.setReward(100);
        
        QuestProgress anotherProgress = new QuestProgress(200, anotherQuest);
        
        assertEquals(200, anotherProgress.getQuestId());
        assertEquals(200, anotherProgress.getQuest().getId());
        assertEquals("Collect", anotherProgress.getQuest().getType());
        assertEquals(0, anotherProgress.getCurrent());
    }

    @Test
    void testIncrementDoesNotAffectOtherInstances() {
        QuestProgress progress2 = new QuestProgress(101, quest);
        
        progress.increment();
        progress.increment();
        
        assertEquals(2, progress.getCurrent());
        assertEquals(0, progress2.getCurrent());
    }
}