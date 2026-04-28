package com.usc.rentbnb.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.usc.rentbnb.models.Island;
import com.usc.rentbnb.models.IslandResponse;
import com.usc.rentbnb.models.Listing;
import com.usc.rentbnb.models.ListingResponse;
import com.usc.rentbnb.network.ApiClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeViewModel extends ViewModel {
    private final MutableLiveData<List<Island>> islands = new MutableLiveData<>();
    private final MutableLiveData<List<Listing>> listings = new MutableLiveData<>();

    // Filtered data for the UI to observe
    private final MutableLiveData<List<Island>> filteredIslands = new MutableLiveData<>();
    private final MutableLiveData<List<Listing>> filteredListings = new MutableLiveData<>();

    public LiveData<List<Island>> getIslands() { return filteredIslands; }
    public LiveData<List<Listing>> getListings() { return filteredListings; }

    public void fetchIslands() {
        ApiClient.getApiService().getIslands().enqueue(new Callback<IslandResponse>() {
            @Override
            public void onResponse(Call<IslandResponse> call, Response<IslandResponse> response) {
                if (response.isSuccessful() && response.body() != null ) {
                    islands.setValue(response.body().getData());
                    filteredIslands.setValue(response.body().getData());
                }
            }
            @Override
            public void onFailure(Call<IslandResponse> call, Throwable t) { t.printStackTrace(); }
        });
    }

    public void fetchListings() {
        ApiClient.getApiService().getListings(null).enqueue(new Callback<ListingResponse>() {
            @Override
            public void onResponse(Call<ListingResponse> call, Response<ListingResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    listings.setValue(response.body().getData());
                    filteredListings.setValue(response.body().getData());
                }
            }
            @Override
            public void onFailure(Call<ListingResponse> call, Throwable t) { }
        });
    }

    public void filterData(String query, boolean isRentals) {
        // 1. Safety check: If the master lists are null, do nothing
        if (islands.getValue() == null || listings.getValue() == null) return;

        if (query == null || query.isEmpty()) {
            filteredIslands.setValue(islands.getValue());
            filteredListings.setValue(listings.getValue());
            return;
        }

        String lowerQuery = query.toLowerCase().trim();

        if (isRentals) {
            List<Listing> filtered = new ArrayList<>();
            for (Listing item : listings.getValue()) {
                // 2. Safety check: Ensure the fields themselves aren't null
                String name = item.getProductName() != null ? item.getProductName().toLowerCase() : "";
                String island = item.getIsland() != null ? item.getIsland().toLowerCase() : "";

                if (name.contains(lowerQuery) || island.contains(lowerQuery)) {
                    filtered.add(item);
                }
            }
            filteredListings.setValue(filtered);
        } else {
            List<Island> filtered = new ArrayList<>();
            for (Island item : islands.getValue()) {
                // 3. Safety check: Ensure Island name isn't null
                String islandName = item.getIslandName() != null ? item.getIslandName().toLowerCase() : "";
                if (islandName.contains(lowerQuery)) {
                    filtered.add(item);
                }
            }
            filteredIslands.setValue(filtered);
        }
    }
}