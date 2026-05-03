package com.usc.rentbnb.viewmodels;

import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

public class AddListingViewModel extends ViewModel {
    public String productName ="";
    public String description = "";

    public String category = "";
    public String address = "";
    public double price = 0.0;
    public String priceUnit = "hourly"; // hourly, daily, weekly, monthly
    public List<String> paymentMethods = new ArrayList<>(); // gcash, paypal, or cash
    public List<String> suggestedActivities = new ArrayList<>();
    public String coverImageUrl = "";
    public List<String> imageUris = new ArrayList<>();
    public String island = "";

    // Penalty related fields
    public double penaltyPrice = 0.0;
    public String penaltyUnit = "hourly";
}
