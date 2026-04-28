package com.usc.rentbnb.network;

import com.usc.rentbnb.network.interceptors.AuthInterceptor;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    private static final String BASE_URL = "https://rentbnb-0wbk.onrender.com/api/v1/"; // http://10.0.2.2:3000/api/v1/ is localhost for Android emulator | https://rentbnb-0wbk.onrender.com/api/v1/ for Render
    private static Retrofit retrofit = null;
    public static Retrofit getClient() {
        if (retrofit == null) {
            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .addInterceptor(new AuthInterceptor())
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create()) // GSON converts JSON to Java objects
                    .build();
        }
        return retrofit;
    }

    public static ApiService getApiService() {
        return getClient().create(ApiService.class);
    }

}
