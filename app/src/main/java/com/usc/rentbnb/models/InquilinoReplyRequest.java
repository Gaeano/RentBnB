package com.usc.rentbnb.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class InquilinoReplyRequest {
    @SerializedName("chatRoomId") private String chatRoomId;
    @SerializedName("renterId") private String renterId;
    @SerializedName("listingTitle") private String listingTitle;
    @SerializedName("listingCategory") private String listingCategory;
    @SerializedName("listingLocation") private String listingLocation;
    @SerializedName("listingPrice") private String listingPrice;
    @SerializedName("listingDescription") private String listingDescription;
    @SerializedName("ownerFaq") private String ownerFaq;
    @SerializedName("history") private List<String> history;
    @SerializedName("question") private String question;

    public InquilinoReplyRequest(String chatRoomId, String renterId, String listingTitle, String listingCategory, String listingLocation, String listingPrice, String listingDescription, String ownerFaq, List<String> history, String question) {
        this.chatRoomId = chatRoomId;
        this.renterId = renterId;
        this.listingTitle = listingTitle;
        this.listingCategory = listingCategory;
        this.listingLocation = listingLocation;
        this.listingPrice = listingPrice;
        this.listingDescription = listingDescription;
        this.ownerFaq = ownerFaq;
        this.history = history;
        this.question = question;
    }
}