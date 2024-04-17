package com.erincinci.swapiproxy.interceptor;

import com.erincinci.swapiproxy.config.RateLimitProperties;
import io.github.bucket4j.distributed.remote.RemoteBucketState;
import io.github.bucket4j.distributed.serialization.InternalSerializationHelper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Optional;

@Component
public class RateLimitHeaderInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(RateLimitHeaderInterceptor.class);

    private final CacheManager cacheManager;
    private final RateLimitProperties properties;

    public RateLimitHeaderInterceptor(CacheManager cacheManager, RateLimitProperties properties) {
        this.cacheManager = cacheManager;
        this.properties = properties;
    }

    @Override
    public boolean preHandle(@NotNull HttpServletRequest request,
                             @NotNull HttpServletResponse response,
                             @NotNull Object handler) throws Exception {
        rateLimitState(request.getRemoteAddr())
                .ifPresent(rateLimitState -> response.setHeader(
                        properties.getHeaderKey(), String.valueOf(rateLimitState.getAvailableTokens())));
        return true;
    }

    private Optional<RemoteBucketState> rateLimitState(String remoteAddr) {
        try {
            Cache cache = cacheManager.getCache(properties.getCacheName());
            Optional.ofNullable(cache).orElseThrow(IllegalStateException::new);

            Cache.ValueWrapper valueWrapper = cache.get(rateLimitCacheKey(remoteAddr));
            return Optional.ofNullable(valueWrapper)
                    .flatMap(value -> Optional.ofNullable(value.get())
                            .map(val -> InternalSerializationHelper.deserializeState((byte[]) val)));
        } catch (IllegalStateException e) {
            logger.error("Invalid rate limit state for addr [{}]", remoteAddr, e);
            return Optional.empty();
        }
    }

    private String rateLimitCacheKey(String remoteAddr) {
        return properties.getCacheKeyPrefix() + remoteAddr;
    }
}
