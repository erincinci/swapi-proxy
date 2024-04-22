package com.erincinci.swapiproxy.controller;

import com.erincinci.swapiproxy.model.BaseEntity;
import com.erincinci.swapiproxy.model.EntityType;
import com.erincinci.swapiproxy.model.Film;
import com.erincinci.swapiproxy.model.Person;
import com.erincinci.swapiproxy.service.EntityService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Optional;

@RestController
@RequestMapping("api")
public class ProxyController {

    private static final Logger logger = LoggerFactory.getLogger(ProxyController.class);

    private final EntityService entityService;

    public ProxyController(EntityService entityService) {
        this.entityService = entityService;
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Fetch a SWAPI Entity",
                    content = { @Content(mediaType = "application/json", schema = @Schema(oneOf = {
                            Person.class,
                            Film.class
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
            @RequestParam(required = false) boolean enrich,
            @PathVariable String type,
            @PathVariable String id) {
        try {
            final EntityService.EntityRequest entityRequest = new EntityService.EntityRequest(
                    enrich, EntityType.fromValue(type), id, request.getRemoteAddr());

            return mapToResponse(entityService.getEntity(entityRequest));
        } catch (IOException e) {
            logger.error("Error in API call", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private ResponseEntity<? extends BaseEntity> mapToResponse(Optional<? extends BaseEntity> entity) {
        return entity.map(ResponseEntity::ok).orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }
}
