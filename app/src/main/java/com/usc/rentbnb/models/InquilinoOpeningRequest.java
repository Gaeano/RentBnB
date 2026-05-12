package com.usc.rentbnb.models;

import com.google.gson.annotations.SerializedName;

public class InquilinoOpeningRequest {
    @SerializedName("chatRoomId") private String chatRoomId;
    @SerializedName("renterId") private String renterId;
    @SerializedName("ownerId") private String ownerId;
    @SerializedName("listingTitle") private String listingTitle;
    @SerializedName("listingCategory") private String listingCategory;
    @SerializedName("listingLocation") private String listingLocation;
    @SerializedName("listingPrice") private String listingPrice;
    @SerializedName("listingDescription") private String listingDescription;
    @SerializedName("ownerFaq") private String ownerFaq;

    public InquilinoOpeningRequest(String chatRoomId, String renterId, String ownerId, String listingTitle, String listingCategory, String listingLocation, String listingPrice, String listingDescription, String ownerFaq) {
        this.chatRoomId = chatRoomId;
        this.renterId = renterId;
        this.ownerId = ownerId;
        this.listingTitle = listingTitle;
        this.listingCategory = listingCategory;
        this.listingLocation = listingLocation;
        this.listingPrice = listingPrice;
        this.listingDescription = listingDescription;
        this.ownerFaq = ownerFaq;
    }
}