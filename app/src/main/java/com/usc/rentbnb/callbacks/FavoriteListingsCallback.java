package com.usc.rentbnb.callbacks;

import com.usc.rentbnb.models.Listing;

import java.util.List;

public interface FavoriteListingsCallback {
    void onSuccess(List<Listing> listings);
    void onError(String errorMessage);
}
