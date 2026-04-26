package com.usc.rentbnb.models;

public class Island {
    private String id;
    private String island_name;
    private String location;
    private double rating;
    private String category;
    private String description;

    public Island(String id, String island_name, String location, double rating, String category, String description) {
        this.id = id;
        this.island_name = island_name;
        this.location = location;
        this.rating = rating;
        this.category = category;
        this.description = description;
    }

    public Island(){}

    public String getId() { return id; }
    public String getIslandName() { return island_name; }
    public String getLocation() { return location; }
    public double getRating() { return rating; }
    public String getCategory() { return category; }
    public String getDescription() {return description;}
}
