package com.usc.rentbnb.models;

public class Booking {
    private String id;
    private String renterId;
    private String ownerId;
    private String listingId;
    private String startDate;
    private String endDate;
    private double totalPrice;
    private String status;
    private Listing listing; // Optionally include listing details

    public Booking() {}

    public String getId() { return id; }
    public String getRenterId() { return renterId; }
    public String getOwnerId() { return ownerId; }
    public String getListingId() { return listingId; }
    public String getStartDate() { return startDate; }
    public String getEndDate() { return endDate; }
    public double getTotalPrice() { return totalPrice; }
    public String getStatus() { return status; }
    public Listing getListing() { return listing; }

    public void setStatus(String status) { this.status = status; }
}
