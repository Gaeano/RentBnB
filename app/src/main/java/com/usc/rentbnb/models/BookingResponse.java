package com.usc.rentbnb.models;

import java.util.List;

public class BookingResponse {
    private boolean success;
    private String message;
    private List<Booking> data;
    private Booking booking;

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public List<Booking> getData() { return data; }
    public Booking getBooking() { return booking; }
}
