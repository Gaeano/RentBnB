package com.usc.rentbnb.models;

public class CreateListingResponse {
    private boolean success;
    private String listingId;
    private String message;

    public boolean isSuccess() {return success;}
    public String getListingId() {return listingId;}
    public String getMessage() {return message;}
}
