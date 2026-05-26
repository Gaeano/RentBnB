package com.usc.rentbnb.models;

import com.google.gson.annotations.SerializedName;

public class Notification {
    @SerializedName("_id")
    private String id;
    
    @SerializedName("userId")
    private String userId;
    
    @SerializedName("message")
    private String message;
    
    @SerializedName("type")
    private String type; // e.g., "booking", "message", "system"
    
    @SerializedName("is_read")
    private boolean isRead;
    
    @SerializedName("timestamp")
    private String timestamp;

    @SerializedName("listingId")
    private String listingId;

    // Optional fields for different notification types
    private String productName;
    private String productCategory;
    private String productImage;
    private String username;
    private String price;
    private String priceUnit;

    public Notification(String id, String message, String type, boolean isRead, String timestamp) {
        this.id = id;
        this.message = message;
        this.type = type;
        this.isRead = isRead;
        this.timestamp = timestamp;
    }

    // Extended constructor for Renter UI
    public Notification(String id, String type, String productName, String productCategory, String productImage, String price, String priceUnit) {
        this.id = id;
        this.type = type;
        this.productName = productName;
        this.productCategory = productCategory;
        this.productImage = productImage;
        this.price = price;
        this.priceUnit = priceUnit;
        this.isRead = false;
        this.timestamp = "Just now";
    }

    public Notification(String id, String type, String username) {
        this.id = id;
        this.type = type;
        this.username = username;
        this.isRead = false;
        this.timestamp = "Just now";
    }

    // Getters
    public String getId() { return id; }
    public String getUserId() { return userId; }
    public String getMessage() { return message; }
    public String getType() { return type; }
    public boolean isRead() { return isRead; }
    public String getTimestamp() { return timestamp; }

    public String getProductName() { return productName; }
    public String getProductCategory() { return productCategory; }
    public String getProductImage() { return productImage; }
    public String getUsername() { return username; }
    public String getPrice() { return price; }
    public String getPriceUnit() { return priceUnit; }
    
    public String getListingId() {
        return listingId != null ? listingId : id;
    }

    // Setter for local UI updates
    public void setRead(boolean read) { isRead = read; }
    public void setListingId(String listingId) { this.listingId = listingId; }
}
