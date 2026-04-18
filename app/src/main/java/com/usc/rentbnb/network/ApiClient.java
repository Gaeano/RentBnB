package com.usc.rentbnb.network;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    private static final String BASE_URL = "http://10.0.2.2:3000/api/v1/"; // 10.0.2.2 is localhost for Android emulator
    private static Retrofit retrofit;
    public static Retrofit getClient() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create()) // GSON converts JSON to Java objects
                    .build();
        }
        return retrofit;
    }

    public static ApiService getApiService() {
        return getClient().create(ApiService.class);
    }

}
