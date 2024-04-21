package com.erincinci.swapiproxy.interceptor;

import com.erincinci.swapiproxy.config.RateLimitProperties;
import com.erincinci.swapiproxy.service.RateLimitService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(RateLimitInterceptor.class);

    private final RateLimitService rateLimitService;
    private final RateLimitProperties properties;

    public RateLimitInterceptor(RateLimitService rateLimitService, RateLimitProperties properties) {
        this.rateLimitService = rateLimitService;
        this.properties = properties;
    }

    @Override
    public boolean preHandle(@NotNull HttpServletRequest request,
                             @NotNull HttpServletResponse response,
                             @NotNull Object handler) throws Exception {
        if (rateLimitService.consumeToken(request.getRemoteAddr())) {
            response.setHeader(
                    properties.getHeaderKey(),
                    String.valueOf(rateLimitService.remainingTokens(request.getRemoteAddr())));
            return true;
        } else {
            logger.warn("Rate limit reached for {}", request.getRemoteAddr());
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.getWriter().println("Rate limit reached");
            return false;
        }
    }
}
