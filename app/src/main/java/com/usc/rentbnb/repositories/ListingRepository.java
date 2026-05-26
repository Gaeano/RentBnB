package com.usc.rentbnb.repositories;

import com.google.firebase.auth.FirebaseAuth;
import com.usc.rentbnb.callbacks.ListingCallback;
import com.usc.rentbnb.models.ListingResponse;
import com.usc.rentbnb.network.ApiClient;
import com.usc.rentbnb.network.ApiService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ListingRepository {

    private ApiService apiService = ApiClient.getApiService();
    private  String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
    public void fetchOwnerListing(ListingCallback callback){

        apiService.getOwnerListings(uid).enqueue(new Callback<ListingResponse>() {
            @Override
            public void onResponse(Call<ListingResponse> call, Response<ListingResponse> response) {
                if (response.isSuccessful() && response.body() != null){
                    callback.onSuccess(response.body().getData());
                } else {
                    callback.onError("Error: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<ListingResponse> call, Throwable t) {
                callback.onError("Network Error: " + t.getMessage());
            }
        });
    }
}
