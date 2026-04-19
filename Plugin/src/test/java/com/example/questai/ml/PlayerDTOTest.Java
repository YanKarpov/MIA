package com.example.questai.ml;

import com.google.gson.Gson;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

class PlayerDTOTest {

    private PlayerDTO playerDTO;

    @BeforeEach
    void setUp() {
        playerDTO = new PlayerDTO(10, 50, 0.75);
    }

    @Test
    void testPlayerDTOCreation() {
        assertNotNull(playerDTO);
    }

    @Test
    void testGetDeaths() {
        assertEquals(10, playerDTO.getDeaths());
    }

    @Test
    void testGetKills() {
        assertEquals(50, playerDTO.getKills());
    }

    @Test
    void testGetSuccessRate() {
        assertEquals(0.75, playerDTO.getSuccessRate(), 0.01);
    }

    @Test
    void testPlayerDTOWithZeroValues() {
        PlayerDTO emptyDTO = new PlayerDTO(0, 0, 0.0);
        
        assertEquals(0, emptyDTO.getDeaths());
        assertEquals(0, emptyDTO.getKills());
        assertEquals(0.0, emptyDTO.getSuccessRate(), 0.01);
    }

    @Test
    void testPlayerDTOWithMaxValues() {
        PlayerDTO maxDTO = new PlayerDTO(1000, 10000, 1.0);
        
        assertEquals(1000, maxDTO.getDeaths());
        assertEquals(10000, maxDTO.getKills());
        assertEquals(1.0, maxDTO.getSuccessRate(), 0.01);
    }

    @Test
    void testPlayerDTOWithNegativeValues() {
        PlayerDTO negativeDTO = new PlayerDTO(-5, -10, -0.5);
        
        assertEquals(-5, negativeDTO.getDeaths());
        assertEquals(-10, negativeDTO.getKills());
        assertEquals(-0.5, negativeDTO.getSuccessRate(), 0.01);
    }

    @Test
    void testJsonSerialization() {
        Gson gson = new Gson();
        String json = gson.toJson(playerDTO);
        
        assertTrue(json.contains("deaths"));
        assertTrue(json.contains("kills"));
        assertTrue(json.contains("success_rate"));
    }

    @Test
    void testJsonDeserialization() {
        Gson gson = new Gson();
        String json = "{\"deaths\":15,\"kills\":60,\"success_rate\":0.85}";
        
        PlayerDTO deserialized = gson.fromJson(json, PlayerDTO.class);
        
        assertEquals(15, deserialized.getDeaths());
        assertEquals(60, deserialized.getKills());
        assertEquals(0.85, deserialized.getSuccessRate(), 0.01);
    }

    @Test
    void testPlayerDTOClassExists() {
        assertNotNull(PlayerDTO.class);
    }
}