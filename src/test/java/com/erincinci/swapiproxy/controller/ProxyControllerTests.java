package com.erincinci.swapiproxy.controller;

import com.erincinci.swapiproxy.ApplicationTests;
import com.fasterxml.jackson.databind.JsonNode;
import io.github.bucket4j.Bucket;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
public class ProxyControllerTests extends ApplicationTests {

    private static final String REMOTE_ADDR_1 = "192.168.0.1";
    private static final String REMOTE_ADDR_2 = "192.168.0.2";

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        cacheManager.getCache(rateLimitProperties.getCacheName()).clear();
    }

    @Test
    @DisplayName("should rate limit for remote address")
    void testRateLimitForRemoteAddr() throws Exception {
        for (int i = 0; i < rateLimitProperties.getCapacity(); i++) {
            mockMvc.perform(get("/api/entity/people/1").with(remoteAddr(REMOTE_ADDR_1)))
                    .andExpect(status().isOk());
        }
        mockMvc.perform(get("/api/entity/people/1").with(remoteAddr(REMOTE_ADDR_1)))
                .andExpect(status().isTooManyRequests());

        mockMvc.perform(get("/api/entity/people/1").with(remoteAddr(REMOTE_ADDR_2)))
                .andExpect(status().isOk())
                .andExpect(header().longValue(rateLimitProperties.getHeaderKey(), limitSpent(1L)));
    }

    @Test
    @DisplayName("should fail on bad entity request")
    void testGetEntityBadType() throws Exception {
        mockMvc.perform(get("/api/entity/bad/1")).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("should fail on bad entities request")
    void testGetEntitiesBadType() throws Exception {
        mockMvc.perform(post("/api/entities/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[{\"type\":\"invalid\",\"ids\":[\"1\",\"2\"]}]"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("should return empty map for empty entities request")
    void testGetEntitiesEmpty() throws Exception {
        mockMvc.perform(post("/api/entities/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[]"))
                .andExpect(status().isOk())
                .andExpect(content().json("{}"));
    }

    @Test
    @DisplayName("should return results for valid entities request")
    void testGetEntitiesValid() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/entities/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[{\"type\":\"people\",\"ids\":[\"1\"]},{\"type\":\"films\",\"ids\":[\"1\"],\"enrich\":true}]"))
                .andExpect(status().isOk())
                .andReturn();

        String resultContent = result.getResponse().getContentAsString();
        JsonNode jsonNode = objectMapper.readTree(resultContent);
        Assertions.assertEquals(2, jsonNode.size());
        Assertions.assertEquals(1, jsonNode.get("PEOPLE").size());
        Assertions.assertEquals(1, jsonNode.get("FILMS").size());
    }

    @Test
    @DisplayName("should return base response when no enrich flag is set")
    void testWithoutEnrichedResponse() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/entity/films/1").with(remoteAddr(REMOTE_ADDR_1)))
                .andExpect(status().isOk())
                .andReturn();
        verifyRateLimitState(REMOTE_ADDR_1, limitSpent(1L));
        Assertions.assertEquals(200, result.getResponse().getStatus());

        String resultContent = result.getResponse().getContentAsString();
        Assertions.assertFalse(resultContent.contains("Shyriiwook"));
        Assertions.assertFalse(resultContent.contains("Luke Skywalker"));
        Assertions.assertFalse(resultContent.contains("Sienar Fleet Systems"));
        Assertions.assertFalse(resultContent.contains("Sand Crawler"));
        Assertions.assertFalse(resultContent.contains("Tatooine"));
    }

    @Test
    @DisplayName("should enrich response if flag is set")
    void testWithEnrichedResponse() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/entity/films/1?enrich=true").with(remoteAddr(REMOTE_ADDR_1)))
                .andExpect(status().isOk())
                .andReturn();
        verifyRateLimitState(REMOTE_ADDR_1, limitSpent(6L));
        Assertions.assertEquals(200, result.getResponse().getStatus());
        String resultContent = result.getResponse().getContentAsString();
        Assertions.assertTrue(resultContent.contains("Luke Skywalker"));

        result = mockMvc.perform(get("/api/entity/people/1?enrich=true").with(remoteAddr(REMOTE_ADDR_1)))
                .andExpect(status().isOk())
                .andReturn();
        verifyRateLimitState(REMOTE_ADDR_1, limitSpent(12L));
        Assertions.assertEquals(200, result.getResponse().getStatus());
        resultContent = result.getResponse().getContentAsString();
        Assertions.assertTrue(resultContent.contains("Shyriiwook")); // species
        Assertions.assertTrue(resultContent.contains("A New Hope")); // film
        Assertions.assertTrue(resultContent.contains("Sienar Fleet Systems")); // starship
        Assertions.assertTrue(resultContent.contains("Sand Crawler")); // vehicle
        Assertions.assertTrue(resultContent.contains("Tatooine")); // planet
    }

    private long limitSpent(long spent) {
        return rateLimitProperties.getCapacity() - spent;
    }

    private void verifyRateLimitState(String remoteAddr, long remainingTokens) {
        Assertions.assertEquals(remainingTokens,
                cacheManager.getCache(rateLimitProperties.getCacheName())
                        .get(remoteAddr, Bucket.class).getAvailableTokens());
    }

    private static RequestPostProcessor remoteAddr(final String remoteAddr) {
        return request -> {
            request.setRemoteAddr(remoteAddr);
            return request;
        };
    }
}
