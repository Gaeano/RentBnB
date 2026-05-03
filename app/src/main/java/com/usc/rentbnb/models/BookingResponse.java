package com.usc.rentbnb.models;
import java.util.List;

public class BookingResponse {

    private boolean success;

    private int count;

    private List<Booking> data;

    public boolean isSuccess() {
        return success;
    }

    public int getCount() {
        return count;
    }

    public List<Booking> getData() {
        return data;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public void setData(List<Booking> data) {
        this.data = data;
    }
}