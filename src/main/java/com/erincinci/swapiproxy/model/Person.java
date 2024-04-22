package com.erincinci.swapiproxy.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class Person extends BaseEntity {

    String name;
    @JsonAlias("birth_year") String birthYear;
    @JsonAlias("eye_color") String eyeColor;
    @JsonAlias("hair_color") String hairColor;
    @JsonAlias("skin_color") String skinColor;
    Gender gender;
    String height;
    String mass;
    @JsonDeserialize(using = EntityIdDeserializer.class)
    Planet homeworld;
    @JsonDeserialize(contentUsing = EntityIdDeserializer.class)
    List<Film> films;
    @JsonDeserialize(contentUsing = EntityIdDeserializer.class)
    List<Species> species;
    @JsonDeserialize(contentUsing = EntityIdDeserializer.class)
    List<Starship> starships;
    @JsonDeserialize(contentUsing = EntityIdDeserializer.class)
    List<Vehicle> vehicles;

    @Getter
    public enum Gender {
        @JsonProperty("unknown") UNKNOWN,
        @JsonProperty("none") NONE,
        @JsonProperty("n/a") NOT_AVAILABLE,
        @JsonProperty("male") MALE,
        @JsonProperty("female") FEMALE,
        @JsonProperty("hermaphrodite") HERMAPHRODITE
    }
}
