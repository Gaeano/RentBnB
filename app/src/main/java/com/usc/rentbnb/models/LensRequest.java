package com.usc.rentbnb.models;

public class LensRequest {
    private final String imageBase64;

    public LensRequest(String imageBase64) {
        this.imageBase64 = imageBase64;
    }

    public String getImageBase64() { return imageBase64; }
}