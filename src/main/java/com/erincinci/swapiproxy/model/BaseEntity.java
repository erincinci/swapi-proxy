package com.erincinci.swapiproxy.model;

import com.erincinci.swapiproxy.exception.BadRequestException;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.time.Instant;
import java.util.List;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public abstract class BaseEntity implements Serializable {

    protected static final int PATH_TYPE_LOC = 2;
    protected static final int PATH_ID_LOC = 3;

    String id;
    Instant created;
    Instant edited;
    @JsonIgnore boolean isEnriched;

    protected static String[] parseUriPaths(String uriString) {
        return URI.create(uriString).getPath().split("/");
    }

    @SuppressWarnings("unchecked")
    protected static <E extends BaseEntity> E newEntity(String typeStr) {
        return switch (EntityType.fromValue(typeStr)) {
            case PEOPLE -> (E) new Person();
            case SPECIES -> (E) new Species();
            case FILMS -> (E) new Film();
            case VEHICLES -> (E) new Vehicle();
            case STARSHIPS -> (E) new Starship();
            case PLANETS -> (E) new Planet();
            default -> throw new BadRequestException("Invalid entity type [%s]".formatted(typeStr));
        };
    }

    // Custom JSON deserializer for deserializing blank entities with API path only
    public static class EntityIdDeserializer<E extends BaseEntity> extends JsonDeserializer<E> {

        @Override
        public E deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
                throws IOException, JacksonException {
            String[] paths = parseUriPaths(jsonParser.getText());
            E entity = BaseEntity.newEntity(paths[PATH_TYPE_LOC]);
            entity.setId(paths[PATH_ID_LOC]);
            return entity;
        }
    }

    // Custom JSON deserializer for parsing comma separated lists
    public static class CommaSeparatedListDeserializer extends JsonDeserializer<List<String>> {

        @Override
        public List<String> deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
                throws IOException, JacksonException {
            return List.of(jsonParser.getText().trim().split(","));
        }
    }
}
