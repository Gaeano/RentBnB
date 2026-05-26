package com.usc.rentbnb.models;

public class EarningsResponse {
    private boolean success;
    private Data data;

    public boolean isSuccess() { return success; }
    public Data getData() { return data; }

    public static class Data {
        private double totalEarnings;
        private double pendingPayouts;

        public double getTotalEarnings() { return totalEarnings; }
        public double getPendingPayouts() { return pendingPayouts; }
    }
}