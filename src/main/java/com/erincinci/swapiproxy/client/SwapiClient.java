package com.erincinci.swapiproxy.client;

import co.infinum.retromock.BodyFactory;
import co.infinum.retromock.meta.Mock;
import co.infinum.retromock.meta.MockCircular;
import co.infinum.retromock.meta.MockResponse;
import co.infinum.retromock.meta.MockResponses;
import com.erincinci.swapiproxy.model.Film;
import com.erincinci.swapiproxy.model.Person;
import org.jetbrains.annotations.NotNull;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

import java.io.IOException;
import java.io.InputStream;

public interface SwapiClient {

    @Mock
    @MockCircular
    @MockResponses({
            @MockResponse(bodyFactory = MockBodyFactory.class, body = "mock-swapi/person-1.json")
    })
    @GET("people/{id}/")
    Call<Person> getPerson(@Path("id") String id);

    @Mock
    @MockCircular
    @MockResponses({
            @MockResponse(bodyFactory = MockBodyFactory.class, body = "mock-swapi/film-1.json")
    })
    @GET("films/{id}/")
    Call<Film> getFilm(@Path("id") String id);

    // Mock body loader for testing purposes
    class MockBodyFactory implements BodyFactory {
        @Override
        public InputStream create(@NotNull String input) throws IOException {
            return MockBodyFactory.class.getClassLoader().getResourceAsStream(input);
        }
    }
}
