package com.usc.rentbnb.repositories;

import android.util.Log;

import com.usc.rentbnb.callbacks.UserProfileCallback;
import com.usc.rentbnb.models.AuthResponse;
import com.usc.rentbnb.models.User;
import com.usc.rentbnb.network.ApiClient;
import com.usc.rentbnb.network.ApiService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserProfileRepository {

    private ApiService apiService = ApiClient.getApiService();

    public void fetchUserData (UserProfileCallback callback){
        apiService.getUserData().enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null){
                    User user = response.body().getUser();
                    callback.onSuccess(user);
                } else {
                    String errorMssg = "Error stauts code: " + response.code();
                    Log.e("UserProfileRepo", errorMssg);
                    callback.onError(errorMssg);
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                Log.e("UserProfileRepo", "Network error: "  + t.getMessage());
                callback.onError(t.getMessage());
            }
        });
    }
}
