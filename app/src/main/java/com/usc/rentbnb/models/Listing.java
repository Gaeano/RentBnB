package com.usc.rentbnb.models;

import java.io.Serializable;
import java.util.List;

public class Listing implements Serializable {
    private String id;
    private String ownerId;
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

    public Listing(String id, String ownerId, String productName, String description, String category, String island, double price, String priceUnit, double rating, int totalReviews, List<String> imageUrls, boolean isNew, boolean isTrending) {
        this.id = id;
        this.ownerId = ownerId;
        this.productName = productName;
        this.description = description;
        this.category = category;
        this.island = island;
        this.price = price;
        this.priceUnit = priceUnit;
        this.rating = rating;
        this.totalReviews = totalReviews;
        this.imageUrls = imageUrls;
        this.isNew = isNew;
        this.isTrending = isTrending;
    }

    public String getId() { return id; }
    public String getOwnerId() { return ownerId; }
    public String getProductName() { return productName; }
    public String getDescription() { return description; }
    public String getCategory() { return category; }
    public String getIsland() { return island; }
    public double getPrice() { return price; }
    public String getPriceUnit() { return priceUnit; }
    public double getRating() { return rating; }
    public int getTotalReviews() { return totalReviews; }
    public List<String> getImageUrls() { return imageUrls; }
    public boolean isNew() { return isNew; }
    public boolean isTrending() { return isTrending; }
}
