package com.usc.rentbnb.models;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.PropertyName;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

public class User {
    private String uid;
    private String displayName;
    private String email;
    private String photoUrl;
    private String phone;
    private UserLocation location;
    private double rating;
    private double totalRatings;
    private double totalEarnings;

    @Exclude
    private String createdAt;

    @PropertyName("createdAt")
    public Object getFirestoreCreatedAt() {
        return null;
    }

    @PropertyName("createdAt")
    public void setFirestoreCreatedAt(Object value) {
        if (value instanceof Timestamp) {
            Timestamp ts = (Timestamp) value;
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            this.createdAt = sdf.format(ts.toDate());
        } else if (value instanceof String) {
            this.createdAt = (String) value;
        }
    }

    public User() {}

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public UserLocation getLocation() {
        return location;
    }

    public void setLocation(UserLocation location) {
        this.location = location;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public double getTotalRatings() {
        return totalRatings;
    }

    public void setTotalRatings(double totalRatings) {
        this.totalRatings = totalRatings;
    }

    public double getTotalEarnings() {
        return totalEarnings;
    }

    public void setTotalEarnings(double totalEarnings) {
        this.totalEarnings = totalEarnings;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public static class UserLocation {
        private String island;
        private String province;

        public UserLocation() {}
        public String getIsland() { return island; }
        public String getProvince() { return province; }
    }
}