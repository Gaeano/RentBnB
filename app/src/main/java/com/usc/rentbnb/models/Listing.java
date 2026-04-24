package com.usc.rentbnb.models;

import java.util.List;

public class Listing {
    //TODO: field names
    private String id;
    private String productName;
    private String description;
    private String category;
    private String island;
    private double price;
    private String priceUnit;
    private double rating;
    private int totalReviews;
    private List<String> imageUrls;
    private boolean isNew;
    private boolean isTrending;

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
    public boolean isNew() {return isNew;}
    public boolean isTrending() {return isTrending;}
}
