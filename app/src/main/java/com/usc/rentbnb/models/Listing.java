package com.usc.rentbnb.models;

import java.util.List;

public class Listing {
    private String id;
    private String productName;
    private String description;
    private String category;
    private String island;
    private double price;
    private String priceUnit;
    private List<String> paymentMethods;
    private List<String> suggestedActivities;
    private List<String> imageUrls;
    private double rating;
    private int totalReviews;
    private int timesRented;
    private String createdAt;

    public Listing(String id, String productName, String description, String category, String island, double price, String priceUnit, double rating, int totalReviews, List<String> paymentMethods, List<String> suggestedActivities, List<String> imageUrls, String createdAt, int timesRented) {
        this.id = id;
        this.productName = productName;
        this.description = description;
        this.category = category;
        this.island = island;
        this.price = price;
        this.priceUnit = priceUnit;
        this.rating = rating;
        this.totalReviews = totalReviews;
        this.paymentMethods = paymentMethods;
        this.suggestedActivities = suggestedActivities;
        this.imageUrls = imageUrls;
        this.createdAt = createdAt;
        this.timesRented = timesRented;
    }

    public Listing() {}

    public String getId() {return id;}
    public String getProductName() {return productName;}
    public String getDescription() {return description;}
    public String getCategory() {return category;}
    public String getIsland() {return island;}
    public double getPrice() {return price;}
    public String getPriceUnit() {return priceUnit;}
    public double getRating() {return rating;}
    public int getTotalReviews() {return totalReviews;}
    public List<String> getImageUrls() {return imageUrls;}
    public String getCreatedAt() {return createdAt;}
    public int getTimesRented() {return timesRented;}
    public List<String> getPaymentMethods() {return paymentMethods;}
    public List<String> getSuggestedActivities() {return suggestedActivities;}
}
