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
    private UserLocation location;
    private double rating;
    private double totalRatings;
    private double totalEarnings;

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

    public User() {}

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

    public double getTotalRatings() {
        return totalRatings;
    }

    public void setTotalRatings(double totalRatings) {
        this.totalRatings = totalRatings;
    }

    public double getTotalEarnings() {
        return totalEarnings;
    }

    public void setTotalEarnings(double totalEarnings) {
        this.totalEarnings = totalEarnings;
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

        public CompanyDetails() {}

        public CompanyDetails(String companyName, String permitNumber, String businessType, String yearsOfOperation) {
            this.companyName = companyName;
            this.permitNumber = permitNumber;
            this.businessType = businessType;
            this.yearsOfOperation = yearsOfOperation;
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
}
