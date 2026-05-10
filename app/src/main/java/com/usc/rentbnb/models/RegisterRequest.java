package com.usc.rentbnb.models;

import com.google.gson.annotations.SerializedName;

public class RegisterRequest {

    @SerializedName("displayName")
    private String displayName;

    @SerializedName("phone")
    private String phone;

    @SerializedName("age")
    private String age;

    @SerializedName("gender")
    private String gender;

    @SerializedName("city")
    private String city;

    @SerializedName("province")
    private String province;

    @SerializedName("completeAddress")
    private String completeAddress;

    @SerializedName("userType")
    private String userType;

    @SerializedName("companyDetails")
    private CompanyDetails companyDetails;

    // 1. Empty Constructor (Used for Company flow where we set fields manually)
    public RegisterRequest() {
    }

    // 2. Parameterized Constructor (Used for the Individual flow)
    public RegisterRequest(String displayName, String phone, String age, String gender, String city, String province, String completeAddress) {
        this.displayName = displayName;
        this.phone = phone;
        this.age = age;
        this.gender = gender;
        this.city = city;
        this.province = province;
        this.completeAddress = completeAddress;
        this.userType = "INDIVIDUAL"; // Set default to Individual
    }

    // --- Getters and Setters ---

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAge() { return age; }
    public void setAge(String age) { this.age = age; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getProvince() { return province; }
    public void setProvince(String province) { this.province = province; }

    public String getCompleteAddress() { return completeAddress; }
    public void setCompleteAddress(String completeAddress) { this.completeAddress = completeAddress; }

    public String getUserType() { return userType; }
    public void setUserType(String userType) { this.userType = userType; }

    public CompanyDetails getCompanyDetails() { return companyDetails; }
    public void setCompanyDetails(CompanyDetails companyDetails) { this.companyDetails = companyDetails; }


    // --- Nested Class for Company-Specific Data ---
    public static class CompanyDetails {

        @SerializedName("companyName")
        private String companyName;

        @SerializedName("permitNumber")
        private String permitNumber;

        @SerializedName("businessType")
        private String businessType;

        @SerializedName("yearsOfOperation")
        private String yearsOfOperation;

        // You can add your radius, specificAreas, and coverage variables here later
        // if you want to store them nested inside the company object on Firestore.

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
    }
}