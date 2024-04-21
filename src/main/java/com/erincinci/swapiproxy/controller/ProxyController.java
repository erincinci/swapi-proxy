package com.erincinci.swapiproxy.controller;

import com.erincinci.swapiproxy.exception.BadRequestException;
import com.erincinci.swapiproxy.model.BaseEntity;
import com.erincinci.swapiproxy.model.EntityType;
import com.erincinci.swapiproxy.model.Film;
import com.erincinci.swapiproxy.service.RateLimitService;
import com.erincinci.swapiproxy.service.SwapiService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URI;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("api")
public class ProxyController {
    private static final Logger logger = LoggerFactory.getLogger(ProxyController.class);

    private final RateLimitService rateLimitService;
    private final SwapiService swapiService;

    public ProxyController(RateLimitService rateLimitService, SwapiService swapiService) {
        this.rateLimitService = rateLimitService;
        this.swapiService = swapiService;
    }

    @GetMapping("/entity/{type}/{id}")
    public ResponseEntity<? extends BaseEntity> getEntity(
            @NotNull HttpServletRequest request,
            @RequestParam(required = false) boolean enrich,
            @PathVariable String type,
            @PathVariable String id) {
        try {
            return mapToResponse(executeEntityRequest(request.getRemoteAddr(), enrich, type, id));
        } catch (IOException e) {
            logger.error("Error in API call", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    public Optional<? extends BaseEntity> executeEntityRequest(
            String remoteAddr, boolean enrich, String type, String id) throws IOException {
        final EntityType entityType = EntityType.fromValue(type);
        return switch (entityType) {
            case PEOPLE -> swapiService.getPerson(id);
            case FILMS -> enrichIfNeeded(remoteAddr, enrich, swapiService.getFilm(id));
            case PLANETS -> Optional.empty();
            case SPECIES -> Optional.empty();
            case VEHICLES -> Optional.empty();
            case STARSHIPS -> Optional.empty();
            default -> throw new BadRequestException();
        };
    }

    @SneakyThrows
    public Optional<Film> enrichIfNeeded(String remoteAddr, boolean enrich, Optional<Film> film) {
        // TODO: GENERIC?
        // TODO: How to change rate-limit value for nested calls?
        if (enrich) {
            int numOfRequests = film.map(value -> value.getPeople().size()).orElse(0);
            if (numOfRequests > 0 && rateLimitService.consumeTokens(remoteAddr, numOfRequests)) {
                film.ifPresent(value -> value.setParsedPeople(
                        value.getPeople().stream()
                                .map(URI::create)
                                .map(uri -> uri.getPath().split("/")[3])
                                .map(swapiService::getPerson)
                                .filter(Optional::isPresent)
                                .map(Optional::get)
                                .collect(Collectors.toList())
                ));
            }
        }
        return film;
    }

    private ResponseEntity<? extends BaseEntity> mapToResponse(Optional<? extends BaseEntity> entity) {
        return entity.map(ResponseEntity::ok).orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }
}
