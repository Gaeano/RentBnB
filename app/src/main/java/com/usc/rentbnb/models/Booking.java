package com.usc.rentbnb.models;

public class Booking {
    private String id;
    private String productName;
    private String category;
    private String ownerName;
    private String price;
    private String imageUrl;
    private String dateRange; // e.g., "Oct 12 - Oct 14, 2024"
    private String status;    // "UPCOMING" or "PAST"

    public Booking(String id, String productName, String category, String ownerName, String price, String imageUrl, String dateRange, String status) {
        this.id = id;
        this.productName = productName;
        this.category = category;
        this.ownerName = ownerName;
        this.price = price;
        this.imageUrl = imageUrl;
        this.dateRange = dateRange;
        this.status = status;
    }

    public String getId() { return id; }
    public String getProductName() { return productName; }
    public String getCategory() { return category; }
    public String getOwnerName() { return ownerName; }
    public String getPrice() { return price; }
    public String getImageUrl() { return imageUrl; }
    public String getDateRange() { return dateRange; }
    public String getStatus() { return status; }
}