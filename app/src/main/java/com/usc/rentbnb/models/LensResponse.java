package com.usc.rentbnb.models;

import java.util.List;

public class LensResponse {
    private boolean success;
    private String  detectedCategory;
    private List<String>  keywords;
    private int     tier;
    private List<Listing> data;
    private String  error;

    public boolean isSuccess()              { return success; }
    public String  getDetectedCategory()    { return detectedCategory; }
    public List<String>  getKeywords()      { return keywords; }
    public int     getTier()                { return tier; }
    public List<Listing> getData()          { return data; }
    public String  getError()               { return error; }
}