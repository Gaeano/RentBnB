package com.usc.rentbnb.repositories;

import android.util.Log;

import com.google.gson.Gson;
import com.usc.rentbnb.callbacks.BasicCallback;
import com.usc.rentbnb.callbacks.ReviewsCallback;
import com.usc.rentbnb.models.Review;
import com.usc.rentbnb.models.ReviewRequest;
import com.usc.rentbnb.models.ReviewsResponse;
import com.usc.rentbnb.network.ApiClient;
import com.usc.rentbnb.network.ApiService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReviewsRepository {

    private ApiService apiService = ApiClient.getApiService();

    public void addReview(ReviewRequest newRequest, BasicCallback callback){
        apiService.addReview(newRequest).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()){
                    Log.d("REVIEWS", "Successfully added review " + response.message());
                    callback.onSuccess("Successfully created review");
                } else {
                    Log.e("REVIEWS", "ERROR: " + response.message() + response.code());
                    callback.onError("Error: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("REVIEWS", "NETWORK ERROR: " + t.getMessage());
                callback.onError(t.getMessage());
            }
        });
    }

    public void fetchReviews(String listingId, ReviewsCallback callback){
        apiService.getListingReview(listingId).enqueue(new Callback<ReviewsResponse>() {
            @Override
            public void onResponse(Call<ReviewsResponse> call, Response<ReviewsResponse> response) {
                if (response.isSuccessful() && response.body() != null){
                    Log.d("REVIEWS", "successfully fetched reviews " + response.message() + response.code());
                    callback.onSuccess(response.body().getReviews());
                    Log.d("REVIEWS_DEBUG", "RAW JSON: " + new Gson().toJson(response.body()));
                } else {
                    Log.e("REVIEWS", "error fetching reviews " + response.message() + response.code());
                    callback.onError(response.message());
                }
            }

            @Override
            public void onFailure(Call<ReviewsResponse> call, Throwable t) {
                Log.e("REVIEWS", "Network error: " + t.getMessage());
                callback.onError(t.getMessage());
            }
        });
    }


}
