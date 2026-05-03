package com.usc.rentbnb.repositories;

import com.usc.rentbnb.callbacks.FavoriteIslandsCallback;
import com.usc.rentbnb.callbacks.FavoriteListingsCallback;
import com.usc.rentbnb.models.Island;
import com.usc.rentbnb.models.IslandResponse;
import com.usc.rentbnb.models.Listing;
import com.usc.rentbnb.models.ListingResponse;
import com.usc.rentbnb.network.ApiClient;
import com.usc.rentbnb.network.ApiService;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FavoritesRepository {
    private final ApiService apiService = ApiClient.getApiService();
    public void fetchFavoriteIslands(String userId, FavoriteIslandsCallback callback){
        apiService.getFavoriteIslands(userId).enqueue(new Callback<IslandResponse>() {

            @Override
            public void onResponse(Call<IslandResponse> call, Response<IslandResponse> response) {
                if (response.isSuccessful()) {
                    if (response.body() != null && response.body().getData() != null) {
                        callback.onSuccess(response.body().getData());
                    } else {
                        callback.onError("No favorite Islands");
                        callback.onSuccess(new ArrayList<>()); // RETURN EMPTY LIST, NOT ERROR
                    }
                } else {
                    callback.onError("Failed to fetch favorite islands");
                }
            }

            @Override
            public void onFailure(Call<IslandResponse> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void fetchFavoriteListings(String userId, FavoriteListingsCallback callback){
        apiService.getFavoriteListings(userId).enqueue(new Callback<ListingResponse>() {
            @Override
            public void onResponse(Call<ListingResponse> call, Response<ListingResponse> response) {
                if (response.isSuccessful()) {
                    if (response.body() != null && response.body().getData() != null) {
                        callback.onSuccess(response.body().getData());
                    } else {
                        callback.onError("No favorite listings");
                        callback.onSuccess(new ArrayList<>()); // RETURN EMPTY LIST, NOT ERROR
                    }
                } else {
                    callback.onError("Failed to fetch favorite islands");
                }
            }

            @Override
            public void onFailure(Call<ListingResponse> call, Throwable t) {
                    callback.onError(t.getMessage());
            }
        });
    }

    public void addFavoriteIsland(String userId, Island newFavorite, FavoriteIslandsCallback callback){
        apiService.addFavoriteIsland(userId, newFavorite).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()){
                    fetchFavoriteIslands(userId, callback);
                } else {
                    callback.onError("Failed to add favorite island");
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void addFavoriteListing(String userId, Listing newFavorite, FavoriteListingsCallback callback){
       apiService.addFavoriteListing(userId, newFavorite).enqueue(new Callback<Void>() {

           @Override
           public void onResponse(Call<Void> call, Response<Void> response) {
               if (response.isSuccessful()){
                   fetchFavoriteListings(userId, callback);
               } else {
                   callback.onError("Failed to add favorite listing" + response.code());
               }
           }

           @Override
           public void onFailure(Call<Void> call, Throwable t) {
                callback.onError(t.getMessage());
           }
       });
    }

    public void removeFavoriteIsland(String userId, String islandId, FavoriteIslandsCallback callback){
        apiService.removeFavoriteIsland(userId, islandId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()){
                    fetchFavoriteIslands(userId, callback);
                } else {
                    callback.onError("Failed to remove favorite island");
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void removeFavoriteListing(String userId, String listingId, FavoriteListingsCallback callback){
        apiService.removeFavoriteListing(userId, listingId).enqueue(new Callback<Void>() {

            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()){
                    fetchFavoriteListings(userId, callback);
                } else {
                    callback.onError("Failed to remove favorite listing");
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

}

