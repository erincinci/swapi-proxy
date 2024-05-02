package com.erincinci.swapiproxy.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class Species extends BaseEntity {

    String name;
    String classification;
    String designation;
    String language;
    @JsonAlias("average_height") String averageHeight;
    @JsonAlias("average_lifespan") String averageLifespanYears;
    @JsonDeserialize(using = CommaSeparatedListDeserializer.class)
    @JsonAlias("eye_colors")List<String> eyeColors;
    @JsonDeserialize(using = CommaSeparatedListDeserializer.class)
    @JsonAlias("hair_colors") List<String> hairColors;
    @JsonDeserialize(using = CommaSeparatedListDeserializer.class)
    @JsonAlias("skin_colors") List<String> skinColors;
    @JsonDeserialize(using = EntityIdDeserializer.class)
    Planet homeworld;
    @JsonDeserialize(contentUsing = EntityIdDeserializer.class)
    List<Person> people;
    @JsonDeserialize(contentUsing = EntityIdDeserializer.class)
    List<Film> films;
}
