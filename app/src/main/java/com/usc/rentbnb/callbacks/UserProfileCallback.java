package com.usc.rentbnb.callbacks;

import com.usc.rentbnb.models.User;

public interface UserProfileCallback {
    void onSuccess(User user);
    void onError(String errorMessage);
}