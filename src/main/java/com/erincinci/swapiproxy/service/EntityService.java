package com.erincinci.swapiproxy.service;

import com.erincinci.swapiproxy.exception.BadRequestException;
import com.erincinci.swapiproxy.model.BaseEntity;
import com.erincinci.swapiproxy.model.EntityType;
import com.erincinci.swapiproxy.model.Film;
import com.erincinci.swapiproxy.model.Person;
import kotlin.jvm.functions.Function2;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static pl.touk.throwing.ThrowingFunction.unchecked;

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

        // If enrich flag is set & entity is fetched & entity is not already enriched (to prevent stack overflow)
        if (request.enrichData && optionalEntity.isPresent() && !optionalEntity.get().isEnriched()) {
            E entity = optionalEntity.get();
            Integer totalRequests = enrichFn.invoke(entity, request);
            entity.setEnriched(true);
            logger.info("Enriched entity [{}] with additional {} requests", request.entityId, totalRequests);
            return Optional.of(entity);
        }

        return optionalEntity;
    }


    // Thrown exceptions are handled gracefully in `ControllerExceptionHandler` layer
    @SneakyThrows
    private int enrichEntity(List<CompletableFuture<Integer>> dataEnrichers) {
        CompletableFuture.allOf(dataEnrichers.toArray(new CompletableFuture[0])).join();
        return dataEnrichers.stream()
                .map(unchecked(CompletableFuture::get))
                .reduce(0, Integer::sum, Integer::sum);
    }

    @Async("entityTaskExecutor")
    protected <E extends BaseEntity> CompletableFuture<Integer> enrichField(EntityRequest request,
                                                                            Supplier<List<E>> dataSupplier,
                                                                            Consumer<List<E>> setterFn,
                                                                            Function<String, Optional<E>> swapiFn) {
        List<E> dataToEnrich = dataSupplier.get();
        int numOfRequests = dataToEnrich.size();

        if (numOfRequests > 0 && rateLimitService.consumeTokens(request.remoteAddr, numOfRequests)) {
            List<E> enrichedData = dataToEnrich.parallelStream()
                    .map(BaseEntity::getId)
                    .map(swapiFn)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .toList();
            setterFn.accept(enrichedData);
        }
        return CompletableFuture.completedFuture(numOfRequests);
    }

    private int enrichFilm(Film entity, EntityRequest request) {
        return enrichEntity(List.of(
                enrichField(request, entity::getPeople, entity::setPeople, swapiService::getPerson))
        );
    }

    private int enrichPerson(Person entity, EntityRequest request) {
        return enrichEntity(List.of(
                enrichField(request, entity::getFilms, entity::setFilms, swapiService::getFilm))
        );
    }

    // TODO: Implement enricher for each remaining entity type
}
