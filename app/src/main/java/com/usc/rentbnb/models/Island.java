package com.usc.rentbnb.models;

public class Island {
    private String id;
    private String island_name;
    private String description;
    private String imageUrl;

    private double lat;
    private double lon;

    private Double distanceKm;

    public Island(String id, String island_name, String description, String imageUrl) {
        this.id = id;
        this.island_name = island_name;
        this.description = description;
        this.imageUrl = imageUrl;
    }

    public Island(){}

    public String getId() { return id; }
    public String getIslandName() { return island_name; }
    public String getDescription() {return description;}
    public String getImageUrl() { return imageUrl; }
    public double getLat()          { return lat; }
    public double getLon()          { return lon; }
    public Double getDistanceKm()   { return distanceKm; }

    public void setLat(double lat) { this.lat = lat; }
    public void setLon(double lon) { this.lon = lon; }
    public void setDistanceKm(Double distanceKm) { this.distanceKm = distanceKm; }

    public String getDistanceLabel() {
        if (distanceKm == null) return null;
        if (distanceKm < 1.0) return "Less than 1 km away";
        return String.format(java.util.Locale.getDefault(), "%.0f km away", distanceKm);
    }
}
