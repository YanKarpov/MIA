package com.example.questai.generator;

import com.example.questai.model.Quest;
import com.example.questai.model.Player;
import org.bukkit.Location;  
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;

import static org.junit.jupiter.api.Assertions.*;

@Disabled("Requires Bukkit API, tested on real server")
class QuestGeneratorTest {

    @Test
    void testGenerateQuestNotNull() {
        Quest quest = QuestGenerator.generateQuest(null, null);
        assertNotNull(quest);
        assertNotNull(quest.getType());
        assertTrue(quest.getAmount() > 0);
    }

    @Test
    void testQuestTypesAreValid() {
        for (int i = 0; i < 10; i++) {
            Quest quest = QuestGenerator.generateQuest(null, null);
            String type = quest.getType();
            assertTrue(type.equals("KILL") || type.equals("COLLECT") || type.equals("BREAK"),
                "Тип должен быть KILL, COLLECT или BREAK, но получено: " + type);
        }
    }
    
    @Test
    void testQuestAmountPositive() {
        for (int i = 0; i < 10; i++) {
            Quest quest = QuestGenerator.generateQuest(null, null);
            assertTrue(quest.getAmount() > 0,
                "Amount должен быть положительным, но получено: " + quest.getAmount());
        }
    }
    
    @Test
    void testQuestRewardPositive() {
        for (int i = 0; i < 10; i++) {
            Quest quest = QuestGenerator.generateQuest(null, null);
            assertTrue(quest.getReward() > 0,
                "Reward должен быть положительным, но получено: " + quest.getReward());
        }
    }
    
    @Test
    void testQuestWithPlayerAndLocation() {
        Player player = null;  // на практике на сервере будет реальный игрок
        Location location = null;  // на практике будет реальная локация
        
        Quest quest = QuestGenerator.generateQuest(player, location);
        assertNotNull(quest);
        assertNotNull(quest.getType());
        assertTrue(quest.getAmount() > 0);
        assertTrue(quest.getReward() > 0);
    }
}