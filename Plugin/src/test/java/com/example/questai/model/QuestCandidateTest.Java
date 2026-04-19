package com.example.questai.model;

import com.example.questai.ml.QuestDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

class QuestCandidateTest {

    private Quest quest;
    private QuestDTO dto;
    private QuestCandidate candidate;

    @BeforeEach
    void setUp() {
        quest = new Quest();
        quest.setId(1);
        quest.setType("Kill");
        quest.setTarget("ZOMBIE");
        quest.setAmount(5);
        quest.setReward(50);
        
        dto = new QuestDTO(5);
        
        candidate = new QuestCandidate(quest, dto);
    }

    @Test
    void testQuestCandidateCreation() {
        assertNotNull(candidate);
        assertNotNull(candidate.getQuest());
        assertNotNull(candidate.getDto());
    }

    @Test
    void testGetQuest() {
        Quest result = candidate.getQuest();
        
        assertEquals(1, result.getId());
        assertEquals("Kill", result.getType());
        assertEquals("ZOMBIE", result.getTarget());
        assertEquals(5, result.getAmount());
        assertEquals(50, result.getReward());
    }

    @Test
    void testGetDto() {
        QuestDTO result = candidate.getDto();
        
        assertEquals(5, result.getAmount());
    }

    @Test
    void testQuestAndDtoAreDifferent() {
        assertNotSame(quest, candidate.getQuest());
        assertSame(quest, candidate.getQuest());
    }

    @Test
    void testQuestCandidateWithNullValues() {
        QuestCandidate emptyCandidate = new QuestCandidate(null, null);
        
        assertNull(emptyCandidate.getQuest());
        assertNull(emptyCandidate.getDto());
    }

    @Test
    void testQuestCandidateWithDifferentQuest() {
        Quest anotherQuest = new Quest();
        anotherQuest.setId(2);
        anotherQuest.setType("Collect");
        anotherQuest.setTarget("DIAMOND");
        anotherQuest.setAmount(3);
        anotherQuest.setReward(100);
        
        QuestDTO anotherDto = new QuestDTO(3);
        
        QuestCandidate anotherCandidate = new QuestCandidate(anotherQuest, anotherDto);
        
        assertEquals(2, anotherCandidate.getQuest().getId());
        assertEquals("Collect", anotherCandidate.getQuest().getType());
        assertEquals(3, anotherCandidate.getDto().getAmount());
    }
}