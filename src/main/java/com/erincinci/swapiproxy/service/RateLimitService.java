package com.erincinci.swapiproxy.service;

import com.erincinci.swapiproxy.config.RateLimitProperties;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.local.LocalBucket;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.Cache;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Service
public class RateLimitService {

    private final RateLimitProperties properties;
    private final Cache rateLimitCache;

    public RateLimitService(RateLimitProperties properties, @Qualifier("rateLimitCache") Cache rateLimitCache) {
        this.properties = properties;
        this.rateLimitCache = rateLimitCache;
    }

    public boolean consumeToken(String remoteAddr) {
        return consumeTokens(remoteAddr, 1L);
    }

    public boolean consumeTokens(String remoteAddr, long tokens) {
        return remoteAddrBucket(remoteAddr).tryConsume(tokens);
    }

    public long remainingTokens(String remoteAddr) {
        return remoteAddrBucket(remoteAddr).getAvailableTokens();
    }

    private Bucket remoteAddrBucket(String remoteAddr) {
        return Optional.ofNullable(rateLimitCache.get(remoteAddr, Bucket.class))
                .orElse(initRateLimitBucket(remoteAddr));
    }

    private Bucket initRateLimitBucket(String remoteAddr) {
        LocalBucket bucket = Bucket.builder()
                .addLimit(Bandwidth.simple(
                        properties.getCapacity(),
                        Duration.of(properties.getTime(),
                                ChronoUnit.valueOf(properties.getTimeUnit().toUpperCase()))))
                .build();
        rateLimitCache.putIfAbsent(remoteAddr, bucket);
        return bucket;
    }
}
