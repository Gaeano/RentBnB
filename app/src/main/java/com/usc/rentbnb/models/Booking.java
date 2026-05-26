package com.usc.rentbnb.models;

import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Booking {
    private String id;
    private String listingId;
    private String listingTitle;
    private String listingImageUrl;
    private String renterId;
    private String ownerId;
    private String status;

    // Populated locally after fetching renter profile from Firestore
    private String renterName;
    private String renterPhotoUrl;

    private Schedule schedule;
    private FinancialSummary financialSummary;
    private RenterDetails renterDetails;

    // Legacy flat fields for BookingAdapter compatibility
    private String productName;
    private String category;
    private String ownerName;
    private String price;
    private String imageUrl;
    private double totalPrice;
    private Listing listing;

    public Booking() {}

    // -------------------------------------------------------------------------
    // Nested — NO @SerializedName annotations.
    // Firestore deserializer uses reflection on field names directly and ignores
    // Gson annotations. Field names already match Firestore document keys, so
    // both Gson (Retrofit) and Firestore toObject() work without annotations.
    // -------------------------------------------------------------------------

    public static class Schedule {
        private Object startDate;
        private Object endDate;
        private int totalDays;

        public String getStartDate() { return convertDateToString(startDate); }
        public String getEndDate() { return convertDateToString(endDate); }
        public int getTotalDays() { return totalDays; }

        private String convertDateToString(Object dateObj) {
            if (dateObj == null) return null;
            if (dateObj instanceof String) return (String) dateObj;
            if (dateObj instanceof Timestamp) {
                Date d = ((Timestamp) dateObj).toDate();
                return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(d);
            }
            return String.valueOf(dateObj);
        }
    }

    public static class FinancialSummary {
        private double totalCharged;
        public double getTotalCharged() { return totalCharged; }
    }

    public static class RenterDetails {
        private String name;
        private String email;
        private String contactNumber;
        private String address;
        private String city;
        private String province;

        public String getName() { return name; }
        public String getEmail() { return email; }
        public String getContactNumber() { return contactNumber; }
        public String getAddress() { return address; }
        public String getCity() { return city; }
        public String getProvince() { return province; }
    }

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------
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

    // Legacy getters
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

    public String getStartDate() {
        if (schedule != null) return schedule.getStartDate();
        return null;
    }
    public String getEndDate() {
        if (schedule != null) return schedule.getEndDate();
        return null;
    }

    // -------------------------------------------------------------------------
    // Setters
    // -------------------------------------------------------------------------
    public void setId(String id) { this.id = id; }
    public void setStatus(String status) { this.status = status; }
    public void setRenterName(String renterName) { this.renterName = renterName; }
    public void setRenterPhotoUrl(String renterPhotoUrl) { this.renterPhotoUrl = renterPhotoUrl; }

    /**
     * Called by RentedOutAdapter and BookingRequestAdapter after a successful
     * Firestore listing lookup. Persists the result onto the object so
     * subsequent RecyclerView rebinds do not re-issue the Firestore call.
     */
    public void setCachedListingTitle(String title) { this.listingTitle = title; }
    public void setCachedListingImageUrl(String url) { this.listingImageUrl = url; }
}