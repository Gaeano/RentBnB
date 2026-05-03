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

    private String productName;
    private String productCategory;
    private String productImage;
    private String username;
    private String price;
    private String priceUnit;

    // Chat related fields
    private String chatRoomId;
    private String ownerId;
    private String renterId;
    private String listingId;

    public Notification(String id, String message, String type, boolean isRead, String timestamp) {
        this.id = id;
        this.message = message;
        this.type = type;
        this.isRead = isRead;
        this.timestamp = timestamp;
    }

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

    public Notification(String id, String type, String chatRoomId, String listingId, String listingTitle, String listingImageUrl, String lastMessage, String senderName, String ownerId, String renterId, boolean isRead) {
        this.id = id;
        this.type = type;
        this.chatRoomId = chatRoomId;
        this.listingId = listingId;
        this.productName = listingTitle;
        this.productImage = listingImageUrl;
        this.message = lastMessage;
        this.username = senderName;
        this.ownerId = ownerId;
        this.renterId = renterId;
        this.isRead = isRead;
        this.timestamp = "Recent";
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

    public String getChatRoomId() { return chatRoomId; }
    public String getOwnerId() { return ownerId; }
    public String getRenterId() { return renterId; }
    public String getListingId() { return listingId; }

    public void setRead(boolean read) { isRead = read; }
}