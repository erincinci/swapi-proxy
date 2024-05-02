package com.erincinci.swapiproxy.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class Starship extends Vehicle {

    String mglt;
    @JsonAlias("hyperdrive_rating") String hyperdriveRating;

    @Override
    public EntityType type() {
        return EntityType.STARSHIPS;
    }
}
