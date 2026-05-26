package com.usc.rentbnb.models;

public class ReviewRequest {
    private String listingId;
    private double rating;
    private  String comment;

    public ReviewRequest(String listingId, double rating, String comment) {
        this.listingId = listingId;
        this.rating = rating;
        this.comment = comment;
    }

    public String getListingId() {
        return listingId;
    }

    public double getRating() {
        return rating;
    }
    public String getComment() {
        return comment;
    }
}
