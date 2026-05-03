package com.usc.rentbnb.models;

public class CompanyRegistrationData {
    // Step 1: Basic Info
    private String companyName = "";
    private String email = "";
    private String password = "";

    // Step 2: Mobile
    private String primaryMobile = "";

    // Step 4: Details
    private String businessType = "";
    private String yearsOfOperation = "";
    private String city = "";
    private String province = "";
    private String businessAddress = "";

    public CompanyRegistrationData() {
    }


    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getPrimaryMobile() { return primaryMobile; }
    public void setPrimaryMobile(String primaryMobile) { this.primaryMobile = primaryMobile; }

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
}