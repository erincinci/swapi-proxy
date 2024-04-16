package com.erincinci.swapiproxy.client;

import com.erincinci.swapiproxy.model.People;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface SwapiClient {

    @GET("people/{id}/")
    Call<People> getPeople(@Path("id") String id);
}
