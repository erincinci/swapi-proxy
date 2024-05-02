package com.erincinci.swapiproxy.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class Film extends BaseEntity {

    String title;
    @JsonAlias("episode_id") Integer episodeId;
    @JsonAlias("opening_crawl") String openingCrawl;
    @JsonAlias("release_date") LocalDate releaseDate;
    String director;
    String producer;
    @JsonDeserialize(contentUsing = EntityIdDeserializer.class)
    List<Species> species;
    @JsonDeserialize(contentUsing = EntityIdDeserializer.class)
    List<Starship> starships;
    @JsonDeserialize(contentUsing = EntityIdDeserializer.class)
    List<Vehicle> vehicles;
    @JsonDeserialize(contentUsing = EntityIdDeserializer.class)
    List<Planet> planets;
    @JsonAlias("characters")
    @JsonDeserialize(contentUsing = EntityIdDeserializer.class)
    List<Person> people;

    @Override
    public EntityType type() {
        return EntityType.FILMS;
    }
}
