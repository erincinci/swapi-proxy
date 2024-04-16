package com.erincinci.swapiproxy.config;

import com.erincinci.swapiproxy.client.SwapiClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

@Configuration
public class ClientConfig {

    @Bean
    public Retrofit retrofit(ClientProperties clientProperties, ObjectMapper objectMapper) {
        return new Retrofit.Builder()
                .baseUrl(clientProperties.getSwapi().baseUrl())
                .addConverterFactory(JacksonConverterFactory.create(objectMapper))
                .build();
    }

    @Bean
    public SwapiClient swapiClient(Retrofit retrofit) {
        return retrofit.create(SwapiClient.class);
    }
}
