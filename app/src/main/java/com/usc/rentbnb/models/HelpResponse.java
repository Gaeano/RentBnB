package com.usc.rentbnb.models;

public class HelpResponse {
    private boolean success;
    private String answer;
    private boolean fallback;

    public boolean isSuccess()  { return success; }
    public String getAnswer()   { return answer; }
    public boolean isFallback() { return fallback; }
}