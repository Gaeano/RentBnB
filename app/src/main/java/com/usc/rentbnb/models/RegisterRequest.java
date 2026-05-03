package com.usc.rentbnb.models;

public class RegisterRequest {
    private String displayName;
    private String phone;
    private String age;
    private String gender;
    private String city;
    private String province;
    private String completeAddress;


    public RegisterRequest(String displayName, String phone, String age, String gender, String city, String province, String completeAddress) {
        this.displayName = displayName;
        this.phone = phone;
        this.age = age;
        this.gender = gender;
        this.city = city;
        this.province = province;
        this.completeAddress = completeAddress;
    }
}
