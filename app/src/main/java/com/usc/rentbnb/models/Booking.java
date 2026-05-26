package com.usc.rentbnb.models;

public class Booking {
    private String id;
    private String listingTitle;
    private String listingImageUrl;
    private String ownerId;
    private String ownerName;
    private String renterId;
    private String listingId;
    private String status;
    private FinancialSummary financialSummary;
    private Schedule schedule;

    // nested classes
    public static class FinancialSummary {
        private double totalCharged;
        public double getTotalCharged() { return totalCharged; }
    }

    public static class Schedule {
        private String startDate;
        private String endDate;
        private int totalDays;
        public String getStartDate() { return startDate; }
        public String getEndDate() { return endDate; }
    }

    // getters
    public String getId() { return id; }
    public String getListingTitle() { return listingTitle; }
    public String getListingImageUrl() { return listingImageUrl; }
    public String getOwnerId() { return ownerId; }

    public String getOwnerName() {
        return ownerName;
    }

    public String getRenterId() { return renterId; }
    public String getListingId() { return listingId; }
    public String getStatus() { return status; }
    public FinancialSummary getFinancialSummary() { return financialSummary; }
    public Schedule getSchedule() { return schedule; }
}