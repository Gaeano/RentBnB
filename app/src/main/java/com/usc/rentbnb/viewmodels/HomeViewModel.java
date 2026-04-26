package com.usc.rentbnb.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.usc.rentbnb.models.Island;
import com.usc.rentbnb.models.IslandResponse;
import com.usc.rentbnb.models.Listing;
import com.usc.rentbnb.models.ListingResponse;
import com.usc.rentbnb.network.ApiClient;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeViewModel extends ViewModel {
    private final MutableLiveData<List<Island>> islands = new MutableLiveData<>();
    private final MutableLiveData<List<Listing>> listings = new MutableLiveData<>();

    private List<Island> allIslands = new ArrayList<>();
    private List<Listing> allListings = new ArrayList<>();

    public LiveData<List<Island>> getIslands() {
        return islands;
    }
    public LiveData<List<Listing>> getListings() {return listings;}

    public void fetchIslands() {
        ApiClient.getApiService().getIslands().enqueue(new Callback<IslandResponse>() {
            @Override
            public void onResponse(Call<IslandResponse> call, Response<IslandResponse> response) {
                if (response.isSuccessful() && response.body() != null ) {
                    // Update this line to save the data for searching
                    allIslands = response.body().getData();
                    islands.setValue(allIslands);
                }
            }

            @Override
            public void onFailure(Call<IslandResponse> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    public void fetchListings() {
        ApiClient.getApiService().getListings(null).enqueue(new Callback<ListingResponse>() {
            @Override
            public void onResponse(Call<ListingResponse> call, Response<ListingResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Update this line to save the data for searching
                    allListings = response.body().getData();
                    listings.setValue(allListings);
                }
            }

            @Override
            public void onFailure(Call<ListingResponse> call, Throwable t) { }
        });
    }
    public void filterIslands(String query) {

        if(query == null || query.isEmpty()){
            islands.setValue(allIslands);
            return;
        }

        List<Island> filtered = new ArrayList<>();
        for(Island island : allIslands){
            if(island.getIslandName() != null && island.getIslandName().toLowerCase().contains(query.toLowerCase())){
                filtered.add(island);
            }
        }
        islands.setValue(filtered);
    }

    public void filterRentals(String query) {
        if (query == null || query.isEmpty()) {
            listings.setValue(allListings);
            return;
        }

        List<Listing> filtered = new ArrayList<>();
        for (Listing listing : allListings) {

            if (listing.getProductName() != null && listing.getProductName().toLowerCase().contains(query.toLowerCase())) {
                filtered.add(listing);
            }
        }
        listings.setValue(filtered);
    }
    }


