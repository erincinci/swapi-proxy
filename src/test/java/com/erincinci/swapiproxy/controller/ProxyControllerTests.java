package com.erincinci.swapiproxy.controller;

import com.erincinci.swapiproxy.ApplicationTests;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.cache.CacheManager;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
public class ProxyControllerTests extends ApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    void setUp() {
        cacheManager.getCache("rate-limit-bucket").clear();
    }

    @Test
    void testRateLimit() throws Exception {
        for (int i = 0; i < 3; i++) {
            mockMvc.perform(get("/api/entity/people/1")).andExpect(status().isOk());
        }
        mockMvc.perform(get("/api/entity/people/1")).andExpect(status().isTooManyRequests());
    }

    @Test
    void testGetEntityBadType() throws Exception {
        mockMvc.perform(get("/api/entity/bad/1")).andExpect(status().isBadRequest());
    }
}
