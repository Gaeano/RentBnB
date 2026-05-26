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
    private Penalties penalties;

    public CreateListingRequest(String productName, String description, String category,
                                String island, double price, String priceUnit,
                                List<String> paymentMethods, List<String> suggestedActivities,
                                List<String> imageUrls, Penalties penalties) {
        this.productName = productName;
        this.description = description;
        this.category = category;
        this.island = island;
        this.price = price;
        this.priceUnit = priceUnit;
        this.paymentMethods = paymentMethods;
        this.suggestedActivities = suggestedActivities;
        this.imageUrls = imageUrls;
        this.penalties = penalties;
    }

    public static class Penalties {
        private String penaltyUnit;
        private double penaltyAmount;

        public Penalties(String penaltyUnit, double penaltyAmount) {
            this.penaltyUnit = penaltyUnit;
            this.penaltyAmount = penaltyAmount;
        }

        public String getPenaltyUnit() { return penaltyUnit; }
        public double getPenaltyAmount() { return penaltyAmount; }
    }

    public String getProductName() { return productName; }
    public String getDescription() { return description; }
    public String getCategory() { return category; }
    public String getIsland() { return island; }
    public double getPrice() { return price; }
    public String getPriceUnit() { return priceUnit; }
    public List<String> getPaymentMethods() { return paymentMethods; }
    public List<String> getSuggestedActivities() { return suggestedActivities; }
    public List<String> getImageUrls() { return imageUrls; }
    public Penalties getPenalties() { return penalties; }
}