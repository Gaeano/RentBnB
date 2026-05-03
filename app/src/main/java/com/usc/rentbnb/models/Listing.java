package com.usc.rentbnb.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Listing {
    @SerializedName("_id")
    private String id;

    @SerializedName("productName")
    private String productName;

    private String description;
    private String category;
    private String island;
    private double price;

    @SerializedName("priceUnit")
    private String priceUnit;

    private double rating;

    @SerializedName("totalReviews")
    private int totalReviews;

    @SerializedName("imageUrls")
    private List<String> imageUrls;

    @SerializedName("is_new")
    private boolean isNew;

    @SerializedName("is_trending")
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
