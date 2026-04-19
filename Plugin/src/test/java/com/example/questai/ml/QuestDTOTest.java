package com.example.questai.ml;

import com.google.gson.Gson;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

class QuestDTOTest {

    private QuestDTO questDTO;

    @BeforeEach
    void setUp() {
        questDTO = new QuestDTO(5);
    }

    @Test
    void testQuestDTOCreation() {
        assertNotNull(questDTO);
    }

    @Test
    void testGetAmount() {
        assertEquals(5, questDTO.getAmount());
    }

    @Test
    void testQuestDTOWithZeroAmount() {
        QuestDTO zeroDTO = new QuestDTO(0);
        assertEquals(0, zeroDTO.getAmount());
    }

    @Test
    void testQuestDTOWithLargeAmount() {
        QuestDTO largeDTO = new QuestDTO(100);
        assertEquals(100, largeDTO.getAmount());
    }

    @Test
    void testQuestDTOWithNegativeAmount() {
        QuestDTO negativeDTO = new QuestDTO(-5);
        assertEquals(-5, negativeDTO.getAmount());
    }

    @Test
    void testJsonSerialization() {
        Gson gson = new Gson();
        String json = gson.toJson(questDTO);
        
        assertTrue(json.contains("amount"));
        assertTrue(json.contains("5"));
    }

    @Test
    void testJsonDeserialization() {
        Gson gson = new Gson();
        String json = "{\"amount\":10}";
        
        QuestDTO deserialized = gson.fromJson(json, QuestDTO.class);
        
        assertEquals(10, deserialized.getAmount());
    }

    @Test
    void testMultipleQuestDTOInstances() {
        QuestDTO dto1 = new QuestDTO(3);
        QuestDTO dto2 = new QuestDTO(7);
        QuestDTO dto3 = new QuestDTO(15);
        
        assertEquals(3, dto1.getAmount());
        assertEquals(7, dto2.getAmount());
        assertEquals(15, dto3.getAmount());
    }

    @Test
    void testQuestDTOClassExists() {
        assertNotNull(QuestDTO.class);
    }
}