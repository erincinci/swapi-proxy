package com.erincinci.swapiproxy.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class CacheConfig {

    @Bean
    public CacheManager cacheManager(CacheProperties cacheProperties, RateLimitProperties rateLimitProperties) {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(caffeineCache(cacheProperties));

        // Cache for rate limits per remote address - no expiry as we'll manage buckets by Bucket4j
        cacheManager.registerCustomCache(rateLimitProperties.getCacheName(), Caffeine.newBuilder().build());

        return cacheManager;
    }

    @Bean("rateLimitCache")
    public Cache rateLimitCache(CacheManager cacheManager, RateLimitProperties properties) {
        return cacheManager.getCache(properties.getCacheName());
    }

    private Caffeine<Object, Object> caffeineCache(CacheProperties properties) {
        return Caffeine.newBuilder().expireAfterWrite(
                properties.getTime(), TimeUnit.valueOf(properties.getTimeUnit().toUpperCase()));
    }
}
