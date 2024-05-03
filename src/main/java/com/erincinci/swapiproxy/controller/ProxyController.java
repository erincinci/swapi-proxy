package com.erincinci.swapiproxy.controller;

import com.erincinci.swapiproxy.exception.BadGatewayException;
import com.erincinci.swapiproxy.exception.RateLimitException;
import com.erincinci.swapiproxy.model.*;
import com.erincinci.swapiproxy.service.EntityService;
import com.erincinci.swapiproxy.service.RateLimitService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("api")
public class ProxyController {

    private static final Logger logger = LoggerFactory.getLogger(ProxyController.class);
    public record EntitiesRequest(EntityType type, List<String> ids, boolean enrich) {}

    private final EntityService entityService;
    private final RateLimitService rateLimitService;

    public ProxyController(EntityService entityService, RateLimitService rateLimitService) {
        this.entityService = entityService;
        this.rateLimitService = rateLimitService;
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Fetch a SWAPI Entity",
                    content = { @Content(mediaType = "application/json", schema = @Schema(oneOf = {
                            Person.class, Species.class, Film.class, Vehicle.class, Starship.class, Planet.class
                    }))})})
    @Parameters({
            @Parameter(name = "type", in = ParameterIn.PATH, description = "Entity type to fetch",
                    required = true, schema = @Schema(implementation = EntityType.class)),
            @Parameter(name = "id", in = ParameterIn.PATH, description = "Entity ID",
                    required = true),
            @Parameter(name = "enrich", in = ParameterIn.QUERY, description = "Option to enrich result")
    })
    @GetMapping("/entity/{type}/{id}")
    public ResponseEntity<? extends BaseEntity> getEntity(
            @NotNull HttpServletRequest request,
            @NotNull HttpServletResponse response,
            @RequestParam(required = false) boolean enrich,
            @PathVariable String type,
            @PathVariable String id) {
        if (!rateLimitService.consumeToken(request.getRemoteAddr())) {
            throw new RateLimitException();
        }

        final EntityService.Request entityRequest = new EntityService.Request(
                enrich, EntityType.fromValue(type), id, request.getRemoteAddr());
        Optional<? extends BaseEntity> optionalEntity = executeRequest(entityRequest);

        rateLimitService.setRateLimitHeader(response, request.getRemoteAddr());
        return mapToResponse(optionalEntity);
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Fetch multiple SWAPI Entities",
                    content = {@Content(mediaType = "application/json")}
            )})
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "",
            content = @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = EntitiesRequest.class))
            )
    )
    @PostMapping("/entities/")
    public ResponseEntity<Map<EntityType, ? extends List<? extends BaseEntity>>> getEntities(
            @NotNull HttpServletRequest request,
            @NotNull HttpServletResponse response,
            @RequestBody List<EntitiesRequest> body) {
        logger.info("Got multi-request: {}", body);
        Map<EntityType, List<? extends BaseEntity>> results = body.parallelStream()
                .flatMap(req -> req.ids().stream()
                        .map(id -> new EntityService.Request(req.enrich(), req.type(), id, request.getRemoteAddr())))
                .collect(Collectors.groupingBy(
                        EntityService.Request::entityType,
                        Collectors.toList()
                )).entrySet().stream()
                .filter(entry -> rateLimitService.consumeTokens(request.getRemoteAddr(), entry.getValue().size()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().stream()
                                .map(this::executeRequest)
                                .filter(Optional::isPresent)
                                .map(Optional::get)
                                .collect(Collectors.toList())
                ));

        rateLimitService.setRateLimitHeader(response, request.getRemoteAddr());
        return ResponseEntity.ok(results);
    }

    private Optional<? extends BaseEntity> executeRequest(EntityService.Request entityRequest) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        try {
            return entityService.getEntity(entityRequest);
        } catch (IOException e) {
            logger.error("Error in API call", e);
            throw new BadGatewayException(e);
        } finally {
            stopWatch.stop();
            logger.info("Exec time for req[{} ID#{}]: {} ms",
                    entityRequest.entityType(), entityRequest.entityId(), stopWatch.getTotalTimeMillis());
        }
    }

    private ResponseEntity<? extends BaseEntity> mapToResponse(Optional<? extends BaseEntity> entity) {
        return entity.map(ResponseEntity::ok).orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }
}
