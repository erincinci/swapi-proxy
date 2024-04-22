package com.erincinci.swapiproxy.service;

import com.erincinci.swapiproxy.client.SwapiClient;
import com.erincinci.swapiproxy.exception.BadGatewayException;
import com.erincinci.swapiproxy.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CachePut;
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

    @CachePut(value = "people", key = "#id")
    public Optional<Person> getPerson(String id) {
        return executeCall(swapiClient.getPerson(id), id);
    }

    @CachePut(value = "species", key = "#id")
    public Optional<Species> getSpecies(String id) {
        return executeCall(swapiClient.getSpecies(id), id);
    }

    @CachePut(value = "films", key = "#id")
    public Optional<Film> getFilm(String id) {
        return executeCall(swapiClient.getFilm(id), id);
    }

    @CachePut(value = "vehicles", key = "#id")
    public Optional<Vehicle> getVehicle(String id) {
        return executeCall(swapiClient.getVehicle(id), id);
    }

    @CachePut(value = "starships", key = "#id")
    public Optional<Starship> getStarship(String id) {
        return executeCall(swapiClient.getStarship(id), id);
    }

    @CachePut(value = "planets", key = "#id")
    public Optional<Planet> getPlanet(String id) {
        return executeCall(swapiClient.getPlanet(id), id);
    }

    private <T extends BaseEntity> Optional<T> executeCall(Call<T> call, String id) {
        try {
            Optional<T> optionalEntity = parseResponse(call.execute());
            optionalEntity.ifPresent(entity -> entity.setId(id));
            return optionalEntity;
        } catch (IOException e) {
            logger.error("Error in external API call: {}", e.getMessage());
            throw new BadGatewayException(e);
        }
    }

    private <T extends BaseEntity> Optional<T> parseResponse(Response<T> response) {
        return response.isSuccessful() ? Optional.ofNullable(response.body()) : Optional.empty();
    }
}
