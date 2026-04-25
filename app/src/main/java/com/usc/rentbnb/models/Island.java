package com.usc.rentbnb.models;

public class Island {
    private String id;
    private String island_name;
    private String location;
    private double rating;
    private String category;
    private String description;

    public Island(){}

    public String getId() { return id; }
    public String getIslandName() { return island_name; }
    public String getLocation() { return location; }
    public double getRating() { return rating; }
    public String getCategory() { return category; }
    public String getDescription() {return description;}
}
