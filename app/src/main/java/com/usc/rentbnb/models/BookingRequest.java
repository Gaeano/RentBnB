package com.usc.rentbnb.models;

public class BookingRequest {

    private String listingId;
    private String ownerId;
    private String startDate;
    private String endDate;
    private int totalDays;
    private FinancialSummary financialSummary;
    private RenterDetails renterDetails;
    private String paymentMethod;
    private String cardLast4;

    public BookingRequest(String listingId, String ownerId, String startDate, String endDate,
                          int totalDays, FinancialSummary financialSummary,
                          RenterDetails renterDetails, String paymentMethod, String cardLast4) {
        this.listingId = listingId;
        this.ownerId = ownerId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.totalDays = totalDays;
        this.financialSummary = financialSummary;
        this.renterDetails = renterDetails;
        this.paymentMethod = paymentMethod;
        this.cardLast4 = cardLast4;
    }

    // Getters
    public String getListingId() { return listingId; }
    public String getOwnerId() { return ownerId; }
    public String getStartDate() { return startDate; }
    public String getEndDate() { return endDate; }
    public int getTotalDays() { return totalDays; }
    public FinancialSummary getFinancialSummary() { return financialSummary; }
    public RenterDetails getRenterDetails() { return renterDetails; }
    public String getPaymentMethod() { return paymentMethod; }
    public String getCardLast4() { return cardLast4; }

    // --- Nested classes serialized as objects in JSON ---

    public static class FinancialSummary {
        private double totalCharged;

        public FinancialSummary(double totalCharged) {
            this.totalCharged = totalCharged;
        }

        public double getTotalCharged() { return totalCharged; }
    }

    public static class RenterDetails {
        private String name;
        private String email;
        private String contactNumber;
        private String address;
        private String city;
        private String province;

        public RenterDetails(String name, String email, String contactNumber,
                             String address, String city, String province) {
            this.name = name;
            this.email = email;
            this.contactNumber = contactNumber;
            this.address = address;
            this.city = city;
            this.province = province;
        }

        public String getName() { return name; }
        public String getEmail() { return email; }
        public String getContactNumber() { return contactNumber; }
        public String getAddress() { return address; }
        public String getCity() { return city; }
        public String getProvince() { return province; }
    }
}