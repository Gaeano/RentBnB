package com.usc.rentbnb.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.usc.rentbnb.callbacks.FavoriteIslandsCallback;
import com.usc.rentbnb.callbacks.FavoriteListingsCallback;
import com.usc.rentbnb.models.Island;
import com.usc.rentbnb.models.Listing;
import com.usc.rentbnb.repositories.FavoritesRepository;

import java.util.List;

public class FavoriteViewModel extends ViewModel {

    private final FavoritesRepository favoritesRepository = new FavoritesRepository();

    private final MutableLiveData<List<Island>> favoriteIslands = new MutableLiveData<>();
    private final MutableLiveData<List<Listing>> favoriteListings = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public LiveData<List<Island>> getFavoriteIslands(){
        return favoriteIslands;
    }

    public LiveData<List<Listing>> getFavoriteListings(){
        return favoriteListings;
    }

    public LiveData<Boolean> getIsLoading(){
        return isLoading;
    }

    public LiveData<String> getErrorMessage(){
        return errorMessage;
    }

    public void loadIslands(String userId){
        isLoading.setValue(true);
        favoritesRepository.fetchFavoriteIslands(userId, new FavoriteIslandsCallback(){
            @Override
            public void onSuccess(List<Island> islands) {
                favoriteIslands.setValue(islands);
                isLoading.setValue(false);
            }

            @Override
            public void onError(String message) {
                errorMessage.setValue(message);
                isLoading.setValue(false);
            }
        });
    }

    public void loadListings(String userId){
        isLoading.setValue(true);
        favoritesRepository.fetchFavoriteListings(userId, new FavoriteListingsCallback(){

            @Override
            public void onSuccess(List<Listing> listings) {
                favoriteListings.setValue(listings);
                isLoading.setValue(false);
            }

            @Override
            public void onError(String message) {
                errorMessage.setValue(message);
                isLoading.setValue(false);
            }
        });
    }

    public void addFavoriteIsland(String userId, Island newFavorite){
        isLoading.setValue(true);
        favoritesRepository.addFavoriteIsland(userId, newFavorite, new FavoriteIslandsCallback(){

            @Override
            public void onSuccess(List<Island> islands) {
                favoriteIslands.setValue(islands);
                isLoading.setValue(false);
            }

            @Override
            public void onError(String message) {
                errorMessage.setValue(message);
                isLoading.setValue(false);
            }
        });
    }

    public void addFavoriteListing(String userId, Listing newFavorite){
        isLoading.setValue(true);
        favoritesRepository.addFavoriteListing(userId, newFavorite, new FavoriteListingsCallback() {
            @Override
            public void onSuccess(List<Listing> listings) {
                favoriteListings.setValue(listings);
                isLoading.setValue(false);
            }

            @Override
            public void onError(String message) {
                errorMessage.setValue(message);
                isLoading.setValue(false);
            }
        });
    }

    public void deleteFavoriteIsland(String userId, String islandId){
        isLoading.setValue(true);
        favoritesRepository.removeFavoriteIsland(userId, islandId, new FavoriteIslandsCallback(){

            @Override
            public void onSuccess(List<Island> islands) {
                favoriteIslands.setValue(islands);
                isLoading.setValue(false);
            }

            @Override
            public void onError(String message) {
                errorMessage.setValue(message);
                isLoading.setValue(false);
            }
        });
    }

    public void deleteFavoriteListing(String userId, String listingId){
        isLoading.setValue(true);
        favoritesRepository.removeFavoriteListing(userId, listingId, new FavoriteListingsCallback(){

            @Override
            public void onSuccess(List<Listing> listings) {
                favoriteListings.setValue(listings);
                isLoading.setValue(false);
            }

            @Override
            public void onError(String message) {
                errorMessage.setValue(message);
                isLoading.setValue(false);
            }
        });
    }


}
