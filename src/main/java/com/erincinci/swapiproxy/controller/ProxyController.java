package com.erincinci.swapiproxy.controller;

import com.erincinci.swapiproxy.model.BaseEntity;
import com.erincinci.swapiproxy.model.EntityType;
import com.erincinci.swapiproxy.service.SwapiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Optional;

@RestController
@RequestMapping("api")
public class ProxyController {
    private static final Logger logger = LoggerFactory.getLogger(ProxyController.class);

    private final SwapiService swapiService;

    public ProxyController(SwapiService swapiService) {
        this.swapiService = swapiService;
    }

    @GetMapping("/entity/{type}/{id}")
    public ResponseEntity<? extends BaseEntity> getEntity(@PathVariable String type, @PathVariable String id) {
        final EntityType entityType = EntityType.fromValue(type);
        try {
            switch (entityType) {
                case PEOPLE:
                    return mapToResponse(swapiService.getPeople(id));
                case FILMS:
                    // TODO: other types
                case PLANETS:
                case SPECIES:
                case VEHICLES:
                case STARSHIPS:
                default:
                    return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
            }
        } catch (IOException e) {
            logger.error("Error in API call", e);
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private ResponseEntity<? extends BaseEntity> mapToResponse(Optional<? extends BaseEntity> entity) {
        return entity.map(ResponseEntity::ok).orElse(new ResponseEntity<>(null, HttpStatus.BAD_GATEWAY));
    }
}
