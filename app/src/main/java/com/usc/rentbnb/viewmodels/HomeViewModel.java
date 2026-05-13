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
import java.util.List;


import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class HomeViewModel extends ViewModel {
    private final MutableLiveData<List<Island>> islands = new MutableLiveData<>();
    private final MutableLiveData<List<Listing>> listings = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();


    // Master lists (Sources of Truth)
    private List<Island> allIslands = new ArrayList<>();
    private List<Listing> allListings = new ArrayList<>();


    public LiveData<List<Island>> getIslands() { return islands; }
    public LiveData<List<Listing>> getListings() { return listings; }
    public LiveData<String> getErrorMessage() { return errorMessage; }


    public void fetchIslands() {
        ApiClient.getApiService().getIslands().enqueue(new Callback<IslandResponse>() {
            @Override
            public void onResponse(Call<IslandResponse> call, Response<IslandResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    allIslands = response.body().getData();
                    islands.setValue(allIslands);
                } else {
                    errorMessage.setValue("Error fetching Islands: " + response.code());
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
                    errorMessage.setValue("Error fetching Rentals: " + response.code());
                }
            }
            @Override
            public void onFailure(Call<ListingResponse> call, Throwable t) {
                errorMessage.setValue("Network Error: " + t.getMessage());
            }
        });
    }


    public void filterIslands(String query) {
        if (query == null || query.isEmpty()) {
            islands.setValue(allIslands);
            return;
        }


        String lowerQuery = query.toLowerCase().trim();
        List<Island> filtered = new ArrayList<>();
        for (Island island : allIslands) {
            if (island.getIslandName() != null && island.getIslandName().toLowerCase().contains(lowerQuery)) {
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


        String lowerQuery = query.toLowerCase().trim();
        List<Listing> filtered = new ArrayList<>();
        for (Listing listing : allListings) {
            // Enhanced search: checks product name OR the island it's on
            boolean matchesName = listing.getProductName() != null && listing.getProductName().toLowerCase().contains(lowerQuery);
            boolean matchesIsland = listing.getIsland() != null && listing.getIsland().toLowerCase().contains(lowerQuery);


            if (matchesName || matchesIsland) {
                filtered.add(listing);
            }
        }
        listings.setValue(filtered);
    }


    public void applyFilters(FilterCriteria criteria) {
        if (allListings == null || allListings.isEmpty()) return;
        List<Listing> result = new ArrayList<>();


        for (Listing listing : allListings) {
            if (listing.getPrice() < criteria.minPrice || listing.getPrice() > criteria.maxPrice) continue;
            if (listing.getRating() < criteria.minRating) continue;


            if (criteria.categories != null && !criteria.categories.isEmpty()) {
                if (!criteria.categories.contains(listing.getCategory())) continue;
            }


            if (criteria.activities != null && !criteria.activities.isEmpty()) {
                boolean hasMatch = false;
                if (listing.getSuggestedActivities() != null) {
                    for (String act : criteria.activities) {
                        if (listing.getSuggestedActivities().contains(act)) {
                            hasMatch = true;
                            break;
                        }
                    }
                }
                if (!hasMatch) continue;
            }


            if (criteria.priceUnit != null && !criteria.priceUnit.equals(listing.getPriceUnit())) continue;


            result.add(listing);
        }
        listings.setValue(result);
    }


    public void clearFilters() {
        listings.setValue(new ArrayList<>(allListings));
    }
}
