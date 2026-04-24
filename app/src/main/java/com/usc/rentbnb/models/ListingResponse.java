package com.usc.rentbnb.models;

import java.util.List;

public class ListingResponse {
    private boolean success;
    private List<Listing> data;
    public boolean isSuccess() {return success;}
    public List<Listing> getData() {return data;}
}
