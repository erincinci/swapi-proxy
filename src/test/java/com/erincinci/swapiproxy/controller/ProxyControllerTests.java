package com.erincinci.swapiproxy.controller;

import com.erincinci.swapiproxy.ApplicationTests;
import com.erincinci.swapiproxy.model.Film;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.bucket4j.Bucket;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
public class ProxyControllerTests extends ApplicationTests {

    private static final String REMOTE_ADDR_1 = "192.168.0.1";
    private static final String REMOTE_ADDR_2 = "192.168.0.2";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        cacheManager.getCache(rateLimitProperties.getCacheName()).clear();
    }

    @Test
    @DisplayName("should rate limit for remote address")
    void testRateLimitForARemoteAddr() throws Exception {
        for (int i = 0; i < 3; i++) {
            mockMvc.perform(get("/api/entity/people/1").with(remoteAddr(REMOTE_ADDR_1)))
                    .andExpect(status().isOk());
        }
        mockMvc.perform(get("/api/entity/people/1").with(remoteAddr(REMOTE_ADDR_1)))
                .andExpect(status().isTooManyRequests());

        mockMvc.perform(get("/api/entity/people/1").with(remoteAddr(REMOTE_ADDR_2)))
                .andExpect(status().isOk())
                .andExpect(header().longValue(rateLimitProperties.getHeaderKey(), 2));
    }

    @Test
    @DisplayName("should fail on bad entity request")
    void testGetEntityBadType() throws Exception {
        mockMvc.perform(get("/api/entity/bad/1")).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("should rate limit with enrichment request")
    void testRateLimitWithEnrichment() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/entity/films/1?enrich=true").with(remoteAddr(REMOTE_ADDR_1)))
                .andExpect(status().isOk())
                .andReturn();
        verifyRateLimitState(REMOTE_ADDR_1, 1L);

        Film film = objectMapper.readValue(result.getResponse().getContentAsString(), Film.class);
        Assertions.assertEquals(1, film.getParsedPeople().size());
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
