package com.usc.rentbnb.models;

import com.google.gson.annotations.SerializedName;

public class Review {
    private String id;
    @SerializedName("listingId")
    private String listingId;
    @SerializedName("reviewerId")
    private String reviewerId;
    @SerializedName("reviewerName")
    private String reviewerName;
    @SerializedName("reviewerPhotoUrl")
    private String reviewerPhotoUrl;
    @SerializedName("rating")
    private double rating; // MUST be a double, not a String
    @SerializedName("comment")
    private String comment;
    @SerializedName("createdAt")
    private String createdAt;

    public Review() {}

    public String getId() { return id; }
    public String getListingId() { return listingId; }
    public String getReviewerId() { return reviewerId; }
    public String getReviewerName() { return reviewerName; }
    public double getRating() { return rating; }
    public String getComment() { return comment; }
    public String getCreatedAt() { return createdAt; }
    public String getReviewerPhotoUrl (){return reviewerPhotoUrl;}
}