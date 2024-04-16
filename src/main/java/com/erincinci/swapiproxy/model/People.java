package com.erincinci.swapiproxy.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.Instant;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class People extends BaseEntity {

    String name;
    @JsonAlias("birth_year") String birthYear;
    @JsonAlias("eye_color") String eyeColor;
    @JsonAlias("hair_color") String hairColor;
    @JsonAlias("skin_color") String skinColor;
    Gender gender;
    Double height;
    Double mass;
    String homeworld; // TODO: Create Planet resource
    List<String> films; // TODO: Create Film resource
    List<String> species; // TODO: Create Specie resource
    List<String> starships; // TODO: Create Starship resource
    List<String> vehicles; // TODO: Create Vehicle resource
    Instant created;
    Instant edited;

    @Getter
    public enum Gender {
        @JsonProperty("unknown") UNKNOWN,
        @JsonProperty("n/a") NOT_AVAILABLE,
        @JsonProperty("male") MALE,
        @JsonProperty("female") FEMALE
    }
}
