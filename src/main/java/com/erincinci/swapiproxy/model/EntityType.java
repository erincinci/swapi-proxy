package com.erincinci.swapiproxy.model;

import lombok.Getter;

@Getter
public enum EntityType {
    UNKNOWN("unknown"),
    PEOPLE("people"),
    FILMS("films"),
    STARSHIPS("starships"),
    VEHICLES("vehicles"),
    SPECIES("species"),
    PLANETS("planets");

    private String value;

    EntityType(String value) {
        this.value = value;
    }

    public static EntityType fromValue(String value) {
        for (EntityType entityType : EntityType.values()) {
            if (entityType.value.equals(value)) {
                return entityType;
            }
        }
        return UNKNOWN;
    }
}
