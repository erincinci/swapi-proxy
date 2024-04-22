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
    List<String> species; // TODO: Create Specie resource
    List<String> starships; // TODO: Create Starship resource
    List<String> vehicles; // TODO: Create Vehicle resource
    List<String> planets; // TODO: Create Vehicle resource
    @JsonAlias("characters")
    @JsonDeserialize(contentUsing = BaseEntity.EntityIdDeserializer.class)
    List<Person> people;
}
