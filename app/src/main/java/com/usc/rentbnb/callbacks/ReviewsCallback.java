package com.usc.rentbnb.callbacks;

import com.usc.rentbnb.models.Review;
import com.usc.rentbnb.models.ReviewsResponse;

import java.util.List;

public interface ReviewsCallback {
    void onSuccess(List<Review> reviews);
    void onError(String errorMssg);

}
