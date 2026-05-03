package com.usc.rentbnb.models;

public class CompanyRegistrationData {
    // Step 1: Basic Info
    private String companyName = "";
    private String email = "";
    private String password = "";

    // Step 2: Mobile
    private String primaryMobile = "";
    private String optionalMobile1 = "";
    private String optionalMobile2 = "";
    private String optionalMobile3 = "";

    // Step 4: Details & Service Area
    private String businessType = "";
    private String yearsOfOperation = "";
    private String city = "";
    private String province = "";
    private String businessAddress = "";
    private String radius = "";
    private String coverage = "";
    private String specificAreas = "";

    public CompanyRegistrationData() {}

    // --- Getters and Setters ---
    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getPrimaryMobile() { return primaryMobile; }
    public void setPrimaryMobile(String primaryMobile) { this.primaryMobile = primaryMobile; }

    public String getOptionalMobile1() { return optionalMobile1; }
    public void setOptionalMobile1(String optionalMobile1) { this.optionalMobile1 = optionalMobile1; }

    public String getOptionalMobile2() { return optionalMobile2; }
    public void setOptionalMobile2(String optionalMobile2) { this.optionalMobile2 = optionalMobile2; }

    public String getOptionalMobile3() { return optionalMobile3; }
    public void setOptionalMobile3(String optionalMobile3) { this.optionalMobile3 = optionalMobile3; }

    public String getBusinessType() { return businessType; }
    public void setBusinessType(String businessType) { this.businessType = businessType; }

    public String getYearsOfOperation() { return yearsOfOperation; }
    public void setYearsOfOperation(String yearsOfOperation) { this.yearsOfOperation = yearsOfOperation; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getProvince() { return province; }
    public void setProvince(String province) { this.province = province; }

    public String getBusinessAddress() { return businessAddress; }
    public void setBusinessAddress(String businessAddress) { this.businessAddress = businessAddress; }

    public String getRadius() { return radius; }
    public void setRadius(String radius) { this.radius = radius; }

    public String getCoverage() { return coverage; }
    public void setCoverage(String coverage) { this.coverage = coverage; }

    public String getSpecificAreas() { return specificAreas; }
    public void setSpecificAreas(String specificAreas) { this.specificAreas = specificAreas; }
}