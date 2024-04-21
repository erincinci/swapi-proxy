package com.erincinci.swapiproxy.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties("cache")
public class CacheProperties {

    private Long time;
    private String timeUnit;
}
