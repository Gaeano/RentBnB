package com.usc.rentbnb.models;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;

public class Review {

    @DocumentId
    private String reviewId;

    private String listingId;
    private String renterId;
    private String renterName;
    private String renterPhotoUrl;
    private float rating;
    private String comment;
    private Timestamp createdAt;

    public Review() {}

    public Review(String listingId, String renterId, String renterName,
                  String renterPhotoUrl, float rating, String comment, Timestamp createdAt) {
        this.listingId = listingId;
        this.renterId = renterId;
        this.renterName = renterName;
        this.renterPhotoUrl = renterPhotoUrl;
        this.rating = rating;
        this.comment = comment;
        this.createdAt = createdAt;
    }

    public String getReviewId() { return reviewId; }
    public void setReviewId(String reviewId) { this.reviewId = reviewId; }

    public String getListingId() { return listingId; }
    public void setListingId(String listingId) { this.listingId = listingId; }

    public String getRenterId() { return renterId; }
    public void setRenterId(String renterId) { this.renterId = renterId; }

    public String getRenterName() { return renterName; }
    public void setRenterName(String renterName) { this.renterName = renterName; }

    public String getRenterPhotoUrl() { return renterPhotoUrl; }
    public void setRenterPhotoUrl(String renterPhotoUrl) { this.renterPhotoUrl = renterPhotoUrl; }

    public float getRating() { return rating; }
    public void setRating(float rating) { this.rating = rating; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}