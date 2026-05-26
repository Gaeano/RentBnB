package com.usc.rentbnb.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.usc.rentbnb.callbacks.ListingCallback;
import com.usc.rentbnb.models.Listing;
import com.usc.rentbnb.repositories.ListingRepository;

import java.util.List;

public class ListingViewModel extends ViewModel {

    private ListingRepository listingRepository = new ListingRepository();

    private MutableLiveData<List<Listing>> listingLiveData = new MutableLiveData<>();
    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private MutableLiveData<String> errorLiveData = new MutableLiveData<>();

    public LiveData<List<Listing>> getListingLiveData(){ return listingLiveData;}
    public LiveData<Boolean> getIsLoading(){return isLoading;}
    public LiveData<String> getErrorLiveData(){return errorLiveData;}

    public void getOwnerListings(){
        isLoading.setValue(true);
        listingRepository.fetchOwnerListing(new ListingCallback() {
            @Override
            public void onSuccess(List<Listing> listingList) {
                isLoading.setValue(false);
                listingLiveData.setValue(listingList);
            }

            @Override
            public void onError(String errorMessage) {
                isLoading.setValue(false);
                errorLiveData.setValue(errorMessage);
            }
        });
    }
}
