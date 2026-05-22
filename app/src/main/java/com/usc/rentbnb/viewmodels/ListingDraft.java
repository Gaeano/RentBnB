package com.usc.rentbnb.viewmodels;

import java.util.ArrayList;
import java.util.List;

public class ListingDraft {
    public String productName  = "";
    public String description  = "";
    public String island       = "";
    public String address      = "";
    public String category     = "";
    public double price        = 0.0;
    public String priceUnit    = "hourly";
    public double penaltyAmount = 0.0;
    public String penaltyUnit   = "";  // valid values: "Hourly", "Daily", "Weekly", "Monthly"
    public List<String> paymentMethods      = new ArrayList<>();
    public List<String> suggestedActivities = new ArrayList<>();
    public List<String> imageUris           = new ArrayList<>(); // local URIs before upload
    public List<String> imageUrls           = new ArrayList<>(); // Cloudinary URLs after upload
}