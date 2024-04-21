package com.erincinci.swapiproxy.config;

import co.infinum.retromock.Retromock;
import com.erincinci.swapiproxy.client.SwapiClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jetbrains.annotations.TestOnly;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
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
    @Primary
    @Profile({"prod", "staging"})
    public SwapiClient swapiClient(Retrofit retrofit) {
        return retrofit.create(SwapiClient.class);
    }

    @TestOnly
    @Profile("test")
    @Bean("mockSwapiClient")
    public SwapiClient mockSwapiClient(Retrofit retrofit) {
        return new Retromock.Builder()
                .retrofit(retrofit)
                .addBodyFactory(new SwapiClient.MockBodyFactory())
                .build()
                .create(SwapiClient.class);
    }
}
