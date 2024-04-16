package com.erincinci.swapiproxy.service;

import com.erincinci.swapiproxy.client.SwapiClient;
import com.erincinci.swapiproxy.model.People;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import retrofit2.Response;

import java.io.IOException;
import java.util.Optional;

@Service
public class SwapiService {

    private static final Logger logger = LoggerFactory.getLogger(SwapiService.class);

    private final SwapiClient swapiClient;

    public SwapiService(SwapiClient swapiClient) {
        this.swapiClient = swapiClient;
    }

    @Cacheable("people")
    public Optional<People> getPeople(String id) throws IOException {
        return parseResponse(swapiClient.getPeople(id).execute());
    }

    private <T> Optional<T> parseResponse(Response<T> response) {
        return response.isSuccessful() ? Optional.ofNullable(response.body()) : Optional.empty();
    }
}
