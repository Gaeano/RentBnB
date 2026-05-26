package com.usc.rentbnb.models;

public class ConfirmReturnResponse {
    private boolean success;
    private String message;
    private boolean requiresPenaltyReview;
    private String penaltyUnit;
    private double penaltyAmount;

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public boolean isRequiresPenaltyReview() { return requiresPenaltyReview; }
    public String getPenaltyUnit() { return penaltyUnit; }
    public double getPenaltyAmount() { return penaltyAmount; }
}