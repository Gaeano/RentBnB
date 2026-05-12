package com.usc.rentbnb.models;

import com.google.gson.annotations.SerializedName;

public class InquilinoResponse {
    @SerializedName("success") private boolean success;
    @SerializedName("text") private String text;
    @SerializedName("error") private String error;

    public boolean isSuccess() { return success; }
    public String getText() { return text; }
    public String getError() { return error; }
}