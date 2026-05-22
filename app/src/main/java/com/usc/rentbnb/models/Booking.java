package com.usc.rentbnb.models;

public class Booking {
    private String id;
    private String productName;
    private String category;
    private String ownerName;
    private String price;
    private String imageUrl;
    private String status;    // "UPCOMING" or "PAST"
    private String renterId;
    private String ownerId;
    private String listingId;
    private String startDate;
    private String endDate;
    private double totalPrice;
    private Listing listing;

    public Booking(String id, String productName, String category, String ownerName, String price, String imageUrl, String dateRange, String status, String renterId, String ownerId, String listingId, String startDate, String endDate, double totalPrice, Listing listing) {
        this.id = id;
        this.productName = productName;
        this.category = category;
        this.ownerName = ownerName;
        this.price = price;
        this.imageUrl = imageUrl;
        this.status = status;
        this.renterId = renterId;
        this.ownerId = ownerId;
        this.listingId = listingId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.totalPrice = totalPrice;
        this.listing = listing;
    }
    public Booking() {}

    public String getId() { return id; }
    public String getRenterId() { return renterId; }
    public String getOwnerId() { return ownerId; }
    public String getListingId() { return listingId; }
    public String getStartDate() { return startDate; }
    public String getEndDate() { return endDate; }
    public double getTotalPrice() { return totalPrice; }
    public String getProductName() { return productName; }
    public String getCategory() { return category; }
    public String getOwnerName() { return ownerName; }
    public String getPrice() { return price; }
    public String getStatus() { return status; }

    public Listing getListing() {
        return listing;
    }

    public void setStatus(String status) { this.status = status; }
}