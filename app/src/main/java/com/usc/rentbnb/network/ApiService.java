package com.usc.rentbnb.network;

import com.usc.rentbnb.models.IslandResponse;
import retrofit2.Call;
import retrofit2.http.GET;

public interface ApiService {
    @GET("islands")
    Call<IslandResponse> getIslands();
}
