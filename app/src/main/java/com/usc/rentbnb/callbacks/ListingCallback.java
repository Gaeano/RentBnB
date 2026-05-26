package com.usc.rentbnb.callbacks;

import com.usc.rentbnb.models.Listing;

import java.util.List;

public interface ListingCallback {
    void onSuccess(List<Listing> listingList);

    void onError(String errorMessage);
}
