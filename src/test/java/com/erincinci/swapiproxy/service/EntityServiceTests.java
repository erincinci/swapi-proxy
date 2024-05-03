package com.erincinci.swapiproxy.service;

import com.erincinci.swapiproxy.ApplicationTests;
import com.erincinci.swapiproxy.model.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

public class EntityServiceTests extends ApplicationTests {

    private static final String TEST_ID = "1";

    @Autowired
    EntityService service;

    @Test
    @DisplayName("should get person entity")
    void testGetPerson() throws Exception {
        Person entity = assertEntity(EntityType.PEOPLE, Person.class);
        Assertions.assertEquals("Luke Skywalker", entity.getName());
    }

    @Test
    @DisplayName("should get film entity")
    void testGetFilm() throws Exception {
        Film entity = assertEntity(EntityType.FILMS, Film.class);
        Assertions.assertEquals("A New Hope", entity.getTitle());
    }

    @Test
    @DisplayName("should get planet entity")
    void testGetPlanet() throws Exception {
        Planet entity = assertEntity(EntityType.PLANETS, Planet.class);
        Assertions.assertEquals("Tatooine", entity.getName());
    }

    @Test
    @DisplayName("should get species entity")
    void testGetSpecies() throws Exception {
        Species entity = assertEntity(EntityType.SPECIES, Species.class);
        Assertions.assertEquals("Wookie", entity.getName());
    }

    @Test
    @DisplayName("should get starship entity")
    void testGetStarship() throws Exception {
        Starship entity = assertEntity(EntityType.STARSHIPS, Starship.class);
        Assertions.assertEquals("Death Star", entity.getName());
    }

    @Test
    @DisplayName("should get vehicle entity")
    void testGetVehicle() throws Exception {
        Vehicle entity = assertEntity(EntityType.VEHICLES, Vehicle.class);
        Assertions.assertEquals("Sand Crawler", entity.getName());
    }

    @SuppressWarnings("unchecked")
    private <E extends BaseEntity> E assertEntity(EntityType type, Class<E> entityClass) throws Exception {
        Optional<? extends BaseEntity> optionalEntity = service.getEntity(request(type));
        Assertions.assertTrue(optionalEntity.isPresent());
        Assertions.assertInstanceOf(entityClass, optionalEntity.get());
        Assertions.assertEquals(TEST_ID, optionalEntity.get().getId());
        return (E) optionalEntity.get();
    }

    private EntityService.Request request(EntityType type) {
        return new EntityService.Request(false, type, TEST_ID, "test-address");
    }

    private EntityService.Request request(boolean enrich, EntityType type, String id) {
        return new EntityService.Request(enrich, type, id, "test-address");
    }
}
