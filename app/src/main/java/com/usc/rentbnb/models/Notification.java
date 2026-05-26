package com.usc.rentbnb.models;

public class Notification {
    // Populated from Firestore document ID by NotificationActivity
    private String id;
    private String userId;
    private String type;
    private String title;
    private String body;
    private String bookingId;
    private String listingId;
    // "read" matches the Firestore field name exactly (no @SerializedName needed)
    private boolean read;
    private String createdAt;

    // Display fields — populated from Firestore or constructed locally
    private String productName;
    private String productCategory;
    private String productImage;
    private String username;
    private String price;
    private String priceUnit;
    private String chatRoomId;
    private String ownerId;
    private String renterId;
    private String message;

    // No-arg constructor required for Gson / Firestore toObject()
    public Notification() {}

    // Backend booking/system notification (from Firestore listener)
    public Notification(String id, String type, String title, String body,
                        String bookingId, String listingId, boolean read, String createdAt) {
        this.id        = id;
        this.type      = type;
        this.title     = title;
        this.body      = body;
        this.bookingId = bookingId;
        this.listingId = listingId;
        this.read      = read;
        this.createdAt = createdAt;
    }

    // New listing notification (constructed locally)
    public Notification(String id, String type, String productName, String productCategory,
                        String productImage, String price, String priceUnit) {
        this.id              = id;
        this.listingId       = id;
        this.type            = type;
        this.productName     = productName;
        this.productCategory = productCategory;
        this.productImage    = productImage;
        this.price           = price;
        this.priceUnit       = priceUnit;
        this.read            = false;
        this.createdAt       = "Just now";
    }

    // Chat notification (constructed locally from ChatRoom)
    public Notification(String id, String type, String chatRoomId, String listingId,
                        String listingTitle, String listingImageUrl, String lastMessage,
                        String senderName, String ownerId, String renterId, boolean read) {
        this.id           = id;
        this.type         = type;
        this.chatRoomId   = chatRoomId;
        this.listingId    = listingId;
        this.productName  = listingTitle;
        this.productImage = listingImageUrl;
        this.message      = lastMessage;
        this.username     = senderName;
        this.ownerId      = ownerId;
        this.renterId     = renterId;
        this.read         = read;
        this.createdAt    = "Recent";
    }

    // Getters
    public String getId()              { return id; }
    public String getUserId()          { return userId; }
    public String getType()            { return type; }
    public String getTitle()           { return title; }
    public String getBody()            { return body; }
    public String getBookingId()       { return bookingId; }
    public String getListingId()       { return listingId; }
    public boolean isRead()            { return read; }
    public String getCreatedAt()       { return createdAt; }
    public String getTimestamp()       { return createdAt; }
    public String getProductName()     { return productName; }
    public String getProductCategory() { return productCategory; }
    public String getProductImage()    { return productImage; }
    public String getUsername()        { return username; }
    public String getPrice()           { return price; }
    public String getPriceUnit()       { return priceUnit; }
    public String getChatRoomId()      { return chatRoomId; }
    public String getOwnerId()         { return ownerId; }
    public String getRenterId()        { return renterId; }
    public String getMessage()         { return message; }

    // Setters
    public void setId(String id)                     { this.id = id; }
    public void setRead(boolean read)                { this.read = read; }
    public void setListingId(String listingId)       { this.listingId = listingId; }
    public void setProductName(String productName)   { this.productName = productName; }
    public void setProductImage(String productImage) { this.productImage = productImage; }
    public void setUsername(String username)         { this.username = username; }
    public void setTitle(String title)               { this.title = title; }
    public void setBody(String body)                 { this.body = body; }
}