package com.usc.rentbnb.models;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.PropertyName;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

public class User {
    private String uid;
    private String displayName; // For individuals, this is their name. For companies, it can be the rep's name or company name.
    private String email;
    private String photoUrl;
    private String age;
    private String gender;
    private String phone;
    private String completeAddress;

    private UserLocation location;
    private double rating;
    private double totalReviews;
    private double totalEarnings;
    private int listingsCount;

    @Exclude
    private String createdAt;

    @PropertyName("createdAt")
    public Object getFirestoreCreatedAt() {
        return null;
    }

    @PropertyName("createdAt")
    public void setFirestoreCreatedAt(Object value) {
        if (value instanceof Timestamp) {
            Timestamp ts = (Timestamp) value;
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            this.createdAt = sdf.format(ts.toDate());
        } else if (value instanceof String) {
            this.createdAt = (String) value;
        }
    }

    private String userType; // Use constants like "INDIVIDUAL" or "COMPANY"
    private CompanyDetails companyDetails; // Will be null for Individual users
    private PayoutMethods payoutMethods;

    public User() {}

    public PayoutMethods getPayoutMethods() {
        return payoutMethods;
    }

    public void setPayoutMethods(PayoutMethods payoutMethods) {
        this.payoutMethods = payoutMethods;
    }

    // Existing getters and setters
    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getCompleteAddress() {
        return completeAddress;
    }

    public void setCompleteAddress(String completeAddress) {
        this.completeAddress = completeAddress;
    }

    public UserLocation getLocation() {
        return location;
    }

    public void setLocation(UserLocation location) {
        this.location = location;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public double getTotalReviews() {
        return totalReviews;
    }

    public void setTotalReviews(double totalReviews) {
        this.totalReviews = totalReviews;
    }

    public double getTotalEarnings() {
        return totalEarnings;
    }

    public void setTotalEarnings(double totalEarnings) {
        this.totalEarnings = totalEarnings;
    }

    public int getListingsCount() {
        return listingsCount;
    }

    public void setListingsCount(int listingsCount) {
        this.listingsCount = listingsCount;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }


    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public CompanyDetails getCompanyDetails() {
        return companyDetails;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public void setCompanyDetails(CompanyDetails companyDetails) {
        this.companyDetails = companyDetails;
    }

    // --- EXISTING NESTED CLASS ---
    public static class UserLocation {
        private String city;
        private String province;

        public UserLocation() {}
        public String getCity() { return city; }
        public String getProvince() { return province; }
        public void setCity(String city) { this.city = city; }
        public void setProvince(String province) { this.province = province; }
    }

    public static class CompanyDetails {
        private String companyName;
        private String permitNumber;
        private String businessType;
        private String yearsOfOperation;
        private boolean isVerified;

        private ServiceArea serviceArea;


        public CompanyDetails() {}

        public CompanyDetails(String companyName, String permitNumber, String businessType, String yearsOfOperation) {
            this.companyName = companyName;
            this.permitNumber = permitNumber;
            this.businessType = businessType;
            this.yearsOfOperation = yearsOfOperation;
        }

        public ServiceArea getServiceArea() {
            return serviceArea;
        }

        public void setServiceArea(ServiceArea serviceArea) {
            this.serviceArea = serviceArea;
        }

        public String getBusinessType() {
            return businessType;
        }

        public void setBusinessType(String businessType) {
            this.businessType = businessType;
        }

        public String getYearsOfOperation() {
            return yearsOfOperation;
        }

        public void setYearsOfOperation(String yearsOfOperation) {
            this.yearsOfOperation = yearsOfOperation;
        }

        public String getCompanyName() { return companyName; }
        public void setCompanyName(String companyName) { this.companyName = companyName; }

        public String getPermitNumber() { return permitNumber; }
        public void setPermitNumber(String permitNumber) { this.permitNumber = permitNumber; }

        public boolean isVerified() { return isVerified; }
        public void setVerified(boolean verified) { isVerified = verified; }
    }

    public static class ServiceArea{
        private String radius;
        private String coverage;
        private String specificAreas;

        public ServiceArea(String radius, String coverage, String specificAreas) {
            this.radius = radius;
            this.coverage = coverage;
            this.specificAreas = specificAreas;
        }

        public String getSpecificAreas() {
            return specificAreas;
        }

        public void setSpecificAreas(String specificAreas) {
            this.specificAreas = specificAreas;
        }

        public String getCoverage() {
            return coverage;
        }

        public void setCoverage(String coverage) {
            this.coverage = coverage;
        }

        public String getRadius() {
            return radius;
        }

        public void setRadius(String radius) {
            this.radius = radius;
        }
    }

    public static class PayoutMethods {
        private GcashDetails gcash;
        private PaypalDetails paypal;

        public PayoutMethods() {}

        public GcashDetails getGcash() { return gcash; }
        public void setGcash(GcashDetails gcash) { this.gcash = gcash; }
        public PaypalDetails getPaypal() { return paypal; }
        public void setPaypal(PaypalDetails paypal) { this.paypal = paypal; }
    }

    public static class GcashDetails {
        private String mobileNumber;

        public GcashDetails() {}
        public GcashDetails(String mobileNumber) { this.mobileNumber = mobileNumber; }
        public String getMobileNumber() { return mobileNumber; }
        public void setMobileNumber(String mobileNumber) { this.mobileNumber = mobileNumber; }
    }

    public static class PaypalDetails {
        private String email;
        private String accountHolderName;

        public PaypalDetails() {}
        public PaypalDetails(String email, String accountHolderName) {
            this.email = email;
            this.accountHolderName = accountHolderName;
        }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getAccountHolderName() { return accountHolderName; }
        public void setAccountHolderName(String accountHolderName) { this.accountHolderName = accountHolderName; }
    }
}
