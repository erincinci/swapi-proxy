package com.erincinci.swapiproxy.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class Starship extends BaseEntity {

    String name;
    String model;
    String mglt;
    String consumables;
    @JsonAlias("starship_class") String starshipClass;
    @JsonDeserialize(using = CommaSeparatedListDeserializer.class)
    List<String> manufacturer;
    @JsonAlias("cost_in_credits") String costInCredits;
    Long length;
    Long crew;
    Long passengers;
    @JsonAlias("max_atmosphering_speed") String maxAtmospheringSpeed;
    @JsonAlias("hyperdrive_rating") String hyperdriveRating;
    @JsonAlias("cargo_capacity") Double cargoCapacityKg;

    @JsonDeserialize(contentUsing = EntityIdDeserializer.class)
    List<Film> films;
    @JsonDeserialize(contentUsing = EntityIdDeserializer.class)
    List<Person> pilots;
}
