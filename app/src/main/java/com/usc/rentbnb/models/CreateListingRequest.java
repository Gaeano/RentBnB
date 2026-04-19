package com.usc.rentbnb.models;

import java.util.List;

public class CreateListingRequest {
    private String productName;
    private String description;
    private String category;
    private String island;
    private double price;
    private String priceUnit;
    private List<String> paymentMethods;
    private List<String> suggestedActivities;
    private List<String> imageUrls;

    public CreateListingRequest(String productName, String description, String category, String island, double price, String priceUnit, List<String> paymentMethods, List<String> suggestedActivities, List<String> imageUrls) {
            this.productName = productName;
            this.description = description;
            this.category = category;
            this.island = island;
            this.price = price;
            this.priceUnit = priceUnit;
            this.paymentMethods = paymentMethods;
            this.suggestedActivities = suggestedActivities;
            this.imageUrls = imageUrls;
    }

}
