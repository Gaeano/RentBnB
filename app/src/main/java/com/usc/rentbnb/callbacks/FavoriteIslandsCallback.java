package com.usc.rentbnb.callbacks;

import com.usc.rentbnb.models.Island;

import java.util.List;

public interface FavoriteIslandsCallback {
    void onSuccess(List<Island> islands);
    void onError(String errorMessage);
}
