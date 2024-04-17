package com.erincinci.swapiproxy.config;

import com.erincinci.swapiproxy.interceptor.RateLimitHeaderInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebAppConfig implements WebMvcConfigurer {

    private final RateLimitHeaderInterceptor rateLimitHeaderInterceptor;

    public WebAppConfig(RateLimitHeaderInterceptor rateLimitHeaderInterceptor) {
        this.rateLimitHeaderInterceptor = rateLimitHeaderInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(rateLimitHeaderInterceptor).addPathPatterns("/api/**");
    }
}
