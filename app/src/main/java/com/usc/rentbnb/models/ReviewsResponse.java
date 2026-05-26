package com.usc.rentbnb.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ReviewsResponse {

    private String message;
    @SerializedName("reviews")
    private List<Review> reviews;

    public String getMessage() {
        return message;
    }
    public List<Review> getReviews(){return reviews;}

}
