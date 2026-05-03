package com.usc.rentbnb.models;

public class Booking {
    private String id;
    private String title;
    private String category;
    private String ownerName;
    private String price;
    private String imageUrl;
    private String dateRange; // e.g., "Oct 12 - Oct 14, 2024"
    private String status;    // "UPCOMING" or "PAST"

    public Booking(String id, String title, String category, String ownerName, String price, String imageUrl, String dateRange, String status) {
        this.id = id;
        this.title = title;
        this.category = category;
        this.ownerName = ownerName;
        this.price = price;
        this.imageUrl = imageUrl;
        this.dateRange = dateRange;
        this.status = status;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getCategory() { return category; }
    public String getOwnerName() { return ownerName; }
    public String getPrice() { return price; }
    public String getImageUrl() { return imageUrl; }
    public String getDateRange() { return dateRange; }
    public String getStatus() { return status; }
}