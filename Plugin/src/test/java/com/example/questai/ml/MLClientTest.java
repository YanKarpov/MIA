package com.example.questai.ml;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MLClientTest {

    @Test
    void testMLClientClassExists() {
        assertNotNull(MLClient.class);
    }

    @Test
    void testMLClientCanBeInstantiated() {
        MLClient client = new MLClient();
        assertNotNull(client);
    }

    @Test
    void testRankMethodExists() {
        try {
            MLClient.class.getMethod("rank", RankRequest.class);
            assertTrue(true);
        } catch (NoSuchMethodException e) {
            fail("rank method not found");
        }
    }

    @Test
    void testRankRequestWithNull() {
        MLClient client = new MLClient();
        try {
            client.rank(null);
            // Если дошло сюда то живём
        } catch (Exception e) {
            fail("Method threw exception: " + e.getMessage());
        }
    }
}