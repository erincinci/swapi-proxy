package com.erincinci.swapiproxy.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class Planet extends BaseEntity {

    String name;
    String gravity;
    String population;
    @JsonAlias("diameter") String diameterKm;
    @JsonAlias("surface_water") String surfaceWaterPercentage;
    @JsonAlias("rotation_period") String rotationPeriodHours;
    @JsonAlias("orbital_period") String orbitalPeriodDays;
    @JsonDeserialize(using = CommaSeparatedListDeserializer.class)
    List<String> climate;
    @JsonDeserialize(using = CommaSeparatedListDeserializer.class)
    List<String> terrain;
    @JsonDeserialize(contentUsing = EntityIdDeserializer.class)
    List<Person> residents;
    @JsonDeserialize(contentUsing = EntityIdDeserializer.class)
    List<Film> films;
}
