package com.usc.rentbnb.models;

public class User {
    private String uid;
    private String displayName; // For individuals, this is their name. For companies, it can be the rep's name or company name.
    private String email;
    private String photoUrl;
    private String phone;
    private UserLocation location;
    private double rating;
    private double totalRatings;
    private double totalEarnings;
    private String createdAt;

    // NEW: Architecture Additions for the Dual Sign-up Flow
    private String userType; // Use constants like "INDIVIDUAL" or "COMPANY"
    private CompanyDetails companyDetails; // Will be null for Individual users

    public User() {}

    // ... [Keep all your existing getters and setters exactly as they are] ...

    // --- NEW GETTERS AND SETTERS ---

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public CompanyDetails getCompanyDetails() {
        return companyDetails;
    }

    public void setCompanyDetails(CompanyDetails companyDetails) {
        this.companyDetails = companyDetails;
    }

    // --- EXISTING NESTED CLASS ---
    public static class UserLocation {
        private String island;
        private String province;

        public UserLocation() {}
        public String getIsland() { return island; }
        public String getProvince() { return province; }
        public void setIsland(String island) { this.island = island; }
        public void setProvince(String province) { this.province = province; }
    }

    // --- NEW NESTED CLASS ---
    public static class CompanyDetails {
        private String companyName;
        private String permitNumber;
        private boolean isVerified; // Teacher will likely want this later for permits

        public CompanyDetails() {}

        public CompanyDetails(String companyName, String permitNumber) {
            this.companyName = companyName;
            this.permitNumber = permitNumber;
            this.isVerified = false; // Default to false until an admin approves the permit
        }

        public String getCompanyName() { return companyName; }
        public void setCompanyName(String companyName) { this.companyName = companyName; }

        public String getPermitNumber() { return permitNumber; }
        public void setPermitNumber(String permitNumber) { this.permitNumber = permitNumber; }

        public boolean isVerified() { return isVerified; }
        public void setVerified(boolean verified) { isVerified = verified; }
    }
}