package com.usc.rentbnb.models;

import java.util.List;

public class NotificationResponse {
    private boolean success;
    private List<Notification> data;

    public boolean isSuccess() { return success; }
    public List<Notification> getData() { return data; }
}
