package com.usc.rentbnb.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.usc.rentbnb.models.FilterCriteria;
import com.usc.rentbnb.models.Island;
import com.usc.rentbnb.models.IslandResponse;
import com.usc.rentbnb.models.Listing;
import com.usc.rentbnb.models.ListingResponse;
import com.usc.rentbnb.network.ApiClient;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeViewModel extends ViewModel {
    private final MutableLiveData<List<Island>> islands = new MutableLiveData<>();
    private final MutableLiveData<List<Listing>> listings = new MutableLiveData<>();

    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    private List<Island> allIslands = new ArrayList<>();
    private List<Listing> allListings = new ArrayList<>();

    public LiveData<List<Island>> getIslands() {
        return islands;
    }
    public LiveData<List<Listing>> getListings() {
        return listings;
    }
    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void fetchIslands() {
        ApiClient.getApiService().getIslands().enqueue(new Callback<IslandResponse>() {
            @Override
            public void onResponse(Call<IslandResponse> call, Response<IslandResponse> response) {
                if (response.isSuccessful() && response.body() != null ) {
                    allIslands = response.body().getData();
                    islands.setValue(allIslands);
                } else {
                    errorMessage.setValue("Server Error fetching Islands: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<IslandResponse> call, Throwable t) {
                errorMessage.setValue("Network Error: " + t.getMessage());
            }
        });
    }

    public void fetchListings() {
        ApiClient.getApiService().getListings(null).enqueue(new Callback<ListingResponse>() {
            @Override
            public void onResponse(Call<ListingResponse> call, Response<ListingResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    allListings = response.body().getData();
                    listings.setValue(allListings);
                } else {
                    errorMessage.setValue("Server Error fetching Rentals: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ListingResponse> call, Throwable t) {
                errorMessage.setValue("Network Error: " + t.getMessage());
            }
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

    public void applyFilters(FilterCriteria criteria) {
        if (allListings == null || allListings.isEmpty()) return;

        List<Listing> result = new ArrayList<>();

        for (Listing listing : allListings) {

            if (listing.getPrice() < criteria.minPrice
                    || listing.getPrice() > criteria.maxPrice) {
                continue;
            }

            if (criteria.categories != null && !criteria.categories.isEmpty()) {
                if (!criteria.categories.contains(listing.getCategory())) {
                    continue;
                }
            }

            if (criteria.priceUnit != null) {
                if (!criteria.priceUnit.equals(listing.getPriceUnit())) {
                    continue;
                }
            }

            result.add(listing);
        }

        if (criteria.sortBy != null) {
            switch (criteria.sortBy) {
                case "most_rented":
                    result.sort((a, b) ->
                            Integer.compare(b.getTimesRented(), a.getTimesRented()));
                    break;
                case "newest":
                    // createdAt is an ISO string — lexicographic sort works for ISO-8601
                    result.sort((a, b) ->
                            b.getCreatedAt().compareTo(a.getCreatedAt()));
                    break;
                case "price_asc":
                    result.sort(Comparator.comparingDouble(Listing::getPrice));
                    break;
                case "price_desc":
                    result.sort((a, b) ->
                            Double.compare(b.getPrice(), a.getPrice()));
                    break;
            }
        }

        listings.setValue(result);
    }

    public void clearFilters() {
        listings.setValue(new ArrayList<>(allListings));
    }
}