package com.example.questai.ml;

import com.google.gson.Gson;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RankResponseTest {

    @Test
    void testJsonDeserialization() {
        Gson gson = new Gson();
        String json = "{\"questIndex\":2,\"score\":0.85}";
        
        RankResponse response = gson.fromJson(json, RankResponse.class);
        
        assertEquals(2, response.getQuestIndex());
        assertEquals(0.85, response.getScore(), 0.01);
    }

    @Test
    void testJsonDeserializationWithZeroValues() {
        Gson gson = new Gson();
        String json = "{\"questIndex\":0,\"score\":0.0}";
        
        RankResponse response = gson.fromJson(json, RankResponse.class);
        
        assertEquals(0, response.getQuestIndex());
        assertEquals(0.0, response.getScore(), 0.01);
    }

    @Test
    void testJsonDeserializationWithNegativeValues() {
        Gson gson = new Gson();
        String json = "{\"questIndex\":-1,\"score\":-0.5}";
        
        RankResponse response = gson.fromJson(json, RankResponse.class);
        
        assertEquals(-1, response.getQuestIndex());
        assertEquals(-0.5, response.getScore(), 0.01);
    }

    @Test
    void testJsonSerialization() {
        Gson gson = new Gson();
        RankResponse response = new RankResponse();
        
        String json = gson.toJson(response);
        
        assertNotNull(json);
    }

    @Test
    void testRankResponseClassExists() {
        assertNotNull(RankResponse.class);
    }
}