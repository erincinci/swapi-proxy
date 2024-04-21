package com.erincinci.swapiproxy.service;

import com.erincinci.swapiproxy.client.SwapiClient;
import com.erincinci.swapiproxy.exception.BadGatewayException;
import com.erincinci.swapiproxy.model.Film;
import com.erincinci.swapiproxy.model.Person;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import retrofit2.Call;
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
    public Optional<Person> getPerson(String id) {
        logger.info("Get person with id {}", id);
        return executeCall(swapiClient.getPerson(id));
    }

    @Cacheable("films")
    public Optional<Film> getFilm(String id) {
        return executeCall(swapiClient.getFilm(id));
    }

    private <T> Optional<T> executeCall(Call<T> call) {
        try {
            return parseResponse(call.execute());
        } catch (IOException e) {
            logger.error("Error in external API call: {}", e.getMessage());
            throw new BadGatewayException(e);
        }
    }

    private <T> Optional<T> parseResponse(Response<T> response) {
        return response.isSuccessful() ? Optional.ofNullable(response.body()) : Optional.empty();
    }
}
