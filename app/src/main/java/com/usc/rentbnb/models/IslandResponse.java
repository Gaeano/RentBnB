package com.usc.rentbnb.models;

import java.util.List;

public class IslandResponse {
    private boolean success;
    private List<Island> data;

    public boolean isSuccess() {return success;}
    public List<Island> getData() {return data;}
}
