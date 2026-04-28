package com.usc.rentbnb.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class FilterCriteria implements Serializable {
    public List<String> categories = new ArrayList<>();
    public List<String> activities = new ArrayList<>();
    public float minPrice = 0f;
    public float maxPrice = 50000f; // Matches RangeSlider valueTo
    public float minRating = 0f;
    public String priceUnit = null;
    public String sortBy = null;

    public boolean isEmpty() {
        return categories.isEmpty() && activities.isEmpty() && minPrice == 0f && maxPrice == 50000f && minRating == 0f;
    }
}
