package com.erincinci.swapiproxy.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties("client")
public class ClientProperties {

    public record CommonClientProperties(String baseUrl, Long connectionTimeout, Long readTimeout, Long rateLimit) {}

    private CommonClientProperties swapi;
}
