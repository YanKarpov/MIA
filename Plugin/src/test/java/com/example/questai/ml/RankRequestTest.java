package com.example.questai.ml;

import com.google.gson.Gson;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

class RankRequestTest {

    private PlayerDTO testPlayer;
    private List<QuestDTO> testCandidates;
    private RankRequest rankRequest;

    @BeforeEach
    void setUp() {
        testPlayer = new PlayerDTO(10, 50, 0.75);
        testCandidates = List.of(
            new QuestDTO(5),
            new QuestDTO(10),
            new QuestDTO(15)
        );
        rankRequest = new RankRequest(testPlayer, testCandidates);
    }

    @Test
    void testRankRequestCreation() {
        assertNotNull(rankRequest);
    }

    @Test
    void testGetPlayer() {
        PlayerDTO result = rankRequest.getPlayer();
        
        assertNotNull(result);
        assertEquals(10, result.getDeaths());
        assertEquals(50, result.getKills());
        assertEquals(0.75, result.getSuccessRate(), 0.01);
    }

    @Test
    void testGetCandidates() {
        List<QuestDTO> result = rankRequest.getCandidates();
        
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals(5, result.get(0).getAmount());
        assertEquals(10, result.get(1).getAmount());
        assertEquals(15, result.get(2).getAmount());
    }

    @Test
    void testRankRequestWithNullPlayer() {
        RankRequest nullPlayerRequest = new RankRequest(null, testCandidates);
        
        assertNull(nullPlayerRequest.getPlayer());
        assertNotNull(nullPlayerRequest.getCandidates());
        assertEquals(3, nullPlayerRequest.getCandidates().size());
    }

    @Test
    void testRankRequestWithNullCandidates() {
        RankRequest nullCandidatesRequest = new RankRequest(testPlayer, null);
        
        assertNotNull(nullCandidatesRequest.getPlayer());
        assertNull(nullCandidatesRequest.getCandidates());
    }

    @Test
    void testRankRequestWithEmptyCandidates() {
        RankRequest emptyCandidatesRequest = new RankRequest(testPlayer, List.of());
        
        assertNotNull(emptyCandidatesRequest.getPlayer());
        assertNotNull(emptyCandidatesRequest.getCandidates());
        assertTrue(emptyCandidatesRequest.getCandidates().isEmpty());
    }

    @Test
    void testRankRequestWithAllNull() {
        RankRequest allNullRequest = new RankRequest(null, null);
        
        assertNull(allNullRequest.getPlayer());
        assertNull(allNullRequest.getCandidates());
    }

    @Test
    void testJsonSerialization() {
        Gson gson = new Gson();
        String json = gson.toJson(rankRequest);
        
        assertTrue(json.contains("player"));
        assertTrue(json.contains("candidates"));
        assertTrue(json.contains("deaths"));
        assertTrue(json.contains("kills"));
        assertTrue(json.contains("success_rate"));
        assertTrue(json.contains("amount"));
    }

    @Test
    void testJsonDeserialization() {
        Gson gson = new Gson();
        String json = "{\"player\":{\"deaths\":20,\"kills\":100,\"success_rate\":0.9},\"candidates\":[{\"amount\":3},{\"amount\":7}]}";
        
        RankRequest deserialized = gson.fromJson(json, RankRequest.class);
        
        assertNotNull(deserialized);
        assertEquals(20, deserialized.getPlayer().getDeaths());
        assertEquals(100, deserialized.getPlayer().getKills());
        assertEquals(0.9, deserialized.getPlayer().getSuccessRate(), 0.01);
        assertEquals(2, deserialized.getCandidates().size());
        assertEquals(3, deserialized.getCandidates().get(0).getAmount());
        assertEquals(7, deserialized.getCandidates().get(1).getAmount());
    }

    @Test
    void testRankRequestClassExists() {
        assertNotNull(RankRequest.class);
    }
}