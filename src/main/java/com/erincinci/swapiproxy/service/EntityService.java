package com.erincinci.swapiproxy.service;

import com.erincinci.swapiproxy.exception.BadRequestException;
import com.erincinci.swapiproxy.model.BaseEntity;
import com.erincinci.swapiproxy.model.EntityType;
import com.erincinci.swapiproxy.model.Film;
import com.erincinci.swapiproxy.model.Person;
import kotlin.jvm.functions.Function2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@Service
public class EntityService {

    private static final Logger logger = LoggerFactory.getLogger(EntityService.class);
    public record EntityRequest(boolean enrichData, EntityType entityType, String entityId, String remoteAddr) {}

    private final SwapiService swapiService;
    private final RateLimitService rateLimitService;

    public EntityService(SwapiService swapiService, RateLimitService rateLimitService) {
        this.swapiService = swapiService;
        this.rateLimitService = rateLimitService;
    }

    public Optional<? extends BaseEntity> getEntity(EntityRequest request) throws IOException {
        return switch (request.entityType) {
            case PEOPLE -> getEnrichedEntity(swapiService::getPerson, this::enrichPerson, request);
            case FILMS -> getEnrichedEntity(swapiService::getFilm, this::enrichFilm, request);
            // TODO: Implement remaining entity types
            default -> throw new BadRequestException("Invalid entity type [%s]".formatted(request.entityType));
        };
    }

    private <E extends BaseEntity> Optional<E> getEnrichedEntity(
            Function<String, Optional<E>> swapiFn, Function2<E, EntityRequest, Integer> enrichFn, EntityRequest request) {
        Optional<E> optionalEntity = swapiFn.apply(request.entityId);

        if (request.enrichData && optionalEntity.isPresent()) {
            E entity = optionalEntity.get();
            Integer totalRequests = enrichFn.invoke(entity, request);
            logger.info("Enriched entity [{}] with additional {} requests", request.entityId, totalRequests);
            return Optional.of(entity);
        }

        return optionalEntity;
    }

    private int enrichFilm(Film film, EntityRequest request) {
        int totalRequests = 0;

        // TODO: More generic?
        int numOfRequests = film.getPeople().size();
        if (numOfRequests > 0 && rateLimitService.consumeTokens(request.remoteAddr, numOfRequests)) {
            List<Person> people = film.getPeople().stream()
                    .map(BaseEntity::getId)
                    .map(swapiService::getPerson)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .toList();
            film.setPeople(people);
            totalRequests += numOfRequests;
        }

        return totalRequests;
    }

    private int enrichPerson(Person person, EntityRequest request) {
        // TODO: Implement
        return 0;
    }
}
