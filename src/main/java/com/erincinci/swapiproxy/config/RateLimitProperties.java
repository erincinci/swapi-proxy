package com.erincinci.swapiproxy.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties("rate-limit")
public class RateLimitProperties extends CacheProperties {

    private String cacheName;
    private String headerKey;
    private Long capacity;
}
