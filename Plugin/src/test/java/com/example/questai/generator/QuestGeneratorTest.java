package com.example.questai.generator;
import org.junit.jupiter.api.Disabled;

import com.example.questai.model.Quest;
import com.example.questai.model.Player;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

@Disabled("Requires Bukkit API, tested on real server")
class QuestGeneratorTest {

    @Test
    void testGenerateQuestNotNull() {
        Quest quest = QuestGenerator.generateQuest();
        assertNotNull(quest);
        assertNotNull(quest.getType());
        assertTrue(quest.getAmount() > 0);
    }

    @Test
    void testQuestTypesAreValid() {
        for (int i = 0; i < 10; i++) {
            Quest quest = QuestGenerator.generateQuest();
            String type = quest.getType();
            assertTrue(type.equals("Kill") || type.equals("Collect") || type.equals("Break"),
                "Тип должен быть Kill, Collect или Break, но получено: " + type);
        }
    }
    
    @Test
    void testQuestAmountPositive() {
        for (int i = 0; i < 10; i++) {
            Quest quest = QuestGenerator.generateQuest();
            assertTrue(quest.getAmount() > 0,
                "Amount должен быть положительным, но получено: " + quest.getAmount());
        }
    }
    
    @Test
    void testQuestRewardPositive() {
        for (int i = 0; i < 10; i++) {
            Quest quest = QuestGenerator.generateQuest();
            assertTrue(quest.getReward() > 0,
                "Reward должен быть положительным, но получено: " + quest.getReward());
        }
    }
}