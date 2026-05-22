package com.usc.rentbnb.models;

public class BookingRequest {
    private String renterId;
    private String ownerId;
    private String listingId;
    private String startDate;
    private String endDate;
    private double totalPrice;
    private String status;

    public BookingRequest(String renterId, String ownerId, String listingId, String startDate, String endDate, double totalPrice, String status) {
        this.renterId = renterId;
        this.ownerId = ownerId;
        this.listingId = listingId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.totalPrice = totalPrice;
        this.status = status;
    }

    // Getters and Setters
    public String getRenterId() { return renterId; }
    public void setRenterId(String renterId) { this.renterId = renterId; }

    public String getOwnerId() { return ownerId; }
    public void setOwnerId(String ownerId) { this.ownerId = ownerId; }

    public String getListingId() { return listingId; }
    public void setListingId(String listingId) { this.listingId = listingId; }

    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }

    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }

    public double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
