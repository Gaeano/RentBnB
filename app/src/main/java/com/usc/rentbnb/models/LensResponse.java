package com.usc.rentbnb.models;

import java.util.List;

public class LensResponse {
    private boolean success;
    private List<String> keywords;
    private String detectedObject;
    private String error;

    public boolean isSuccess()         { return success; }
    public List<String> getKeywords()  { return keywords; }
    public String getDetectedObject()  { return detectedObject; }
    public String getError()           { return error; }
}