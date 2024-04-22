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
    String homeworld; // TODO: Create Planet resource
    @JsonDeserialize(contentUsing = BaseEntity.EntityIdDeserializer.class)
    List<Film> films;
    List<String> species; // TODO: Create Specie resource
    List<String> starships; // TODO: Create Starship resource
    List<String> vehicles; // TODO: Create Vehicle resource

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
