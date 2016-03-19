package com.jcanseco.radio.api;

import com.jcanseco.radio.models.RadioContent;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;

public interface RadioRestService {

    @GET("/api")
    Call<RadioContent> getRadioContent();


    class Factory {
        public static RadioRestService create() {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("https://r-a-d.io")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            return retrofit.create(RadioRestService.class);
        }
    }
}
