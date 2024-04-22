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
    Double gravity;
    Long population;
    @JsonAlias("diameter") Double diameterKm;
    @JsonAlias("surface_water") Double surfaceWaterPercentage;
    @JsonAlias("rotation_period") Double rotationPeriodHours;
    @JsonAlias("orbital_period") Double orbitalPeriodDays;
    @JsonDeserialize(using = CommaSeparatedListDeserializer.class)
    List<String> climate;
    @JsonDeserialize(using = CommaSeparatedListDeserializer.class)
    List<String> terrain;
    @JsonDeserialize(contentUsing = EntityIdDeserializer.class)
    List<Person> residents;
    @JsonDeserialize(contentUsing = EntityIdDeserializer.class)
    List<Film> films;
}
