package com.usc.rentbnb.models;

import com.google.gson.annotations.SerializedName;

public class Booking {
    private String id;
    private String listingId;
    private String listingTitle;
    private String listingImageUrl;
    private String renterId;
    private String ownerId;
    private String status;

    // Populated locally after fetching from Firestore users collection
    private String renterName;
    private String renterPhotoUrl;

    private Schedule schedule;
    private FinancialSummary financialSummary;
    private RenterDetails renterDetails;

    // Legacy flat fields kept for backward compatibility with BookingAdapter
    private String productName;
    private String category;
    private String ownerName;
    private String price;
    private String imageUrl;
    private double totalPrice;
    private Listing listing;

    public Booking() {}

    // ---------------------------------------------------------------------------
    // Nested Schedule
    // ---------------------------------------------------------------------------
    public static class Schedule {
        @SerializedName("startDate")
        private String startDate;
        @SerializedName("endDate")
        private String endDate;
        @SerializedName("totalDays")
        private int totalDays;

        public String getStartDate() { return startDate; }
        public String getEndDate() { return endDate; }
        public int getTotalDays() { return totalDays; }
    }

    // ---------------------------------------------------------------------------
    // Nested FinancialSummary
    // ---------------------------------------------------------------------------
    public static class FinancialSummary {
        @SerializedName("totalCharged")
        private double totalCharged;

        public double getTotalCharged() { return totalCharged; }
    }

    // ---------------------------------------------------------------------------
    // Nested RenterDetails
    // ---------------------------------------------------------------------------
    public static class RenterDetails {
        @SerializedName("name")
        private String name;
        @SerializedName("email")
        private String email;
        @SerializedName("contactNumber")
        private String contactNumber;
        @SerializedName("address")
        private String address;
        @SerializedName("city")
        private String city;
        @SerializedName("province")
        private String province;

        public String getName() { return name; }
        public String getEmail() { return email; }
        public String getContactNumber() { return contactNumber; }
        public String getAddress() { return address; }
        public String getCity() { return city; }
        public String getProvince() { return province; }
    }

    // ---------------------------------------------------------------------------
    // Getters
    // ---------------------------------------------------------------------------
    public String getId() { return id; }
    public String getListingId() { return listingId; }
    public String getListingTitle() { return listingTitle; }
    public String getListingImageUrl() { return listingImageUrl; }
    public String getRenterId() { return renterId; }
    public String getOwnerId() { return ownerId; }
    public String getStatus() { return status; }
    public String getRenterName() { return renterName; }
    public String getRenterPhotoUrl() { return renterPhotoUrl; }
    public Schedule getSchedule() { return schedule; }
    public FinancialSummary getFinancialSummary() { return financialSummary; }
    public RenterDetails getRenterDetails() { return renterDetails; }

    // Legacy getters preserved for BookingAdapter compatibility
    public String getProductName() { return productName != null ? productName : listingTitle; }
    public String getCategory() { return category; }
    public String getOwnerName() { return ownerName; }
    public String getPrice() { return price; }
    public String getImageUrl() { return imageUrl != null ? imageUrl : listingImageUrl; }
    public double getTotalPrice() {
        if (financialSummary != null) return financialSummary.getTotalCharged();
        return totalPrice;
    }
    public Listing getListing() { return listing; }

    // Convenience: flat start/end for legacy callers
    public String getStartDate() {
        if (schedule != null) return schedule.getStartDate();
        return null;
    }
    public String getEndDate() {
        if (schedule != null) return schedule.getEndDate();
        return null;
    }

    // ---------------------------------------------------------------------------
    // Setters
    // ---------------------------------------------------------------------------
    public void setId(String id) { this.id = id; }
    public void setStatus(String status) { this.status = status; }
    public void setRenterName(String renterName) { this.renterName = renterName; }
    public void setRenterPhotoUrl(String renterPhotoUrl) { this.renterPhotoUrl = renterPhotoUrl; }
}