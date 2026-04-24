package com.usc.rentbnb.models;

public class WeatherResponse {
    private boolean success;
    private WeatherData data;

    public boolean isSuccess() {return success;}
    public WeatherData getData() {return data;}

    public static class WeatherData {
        private double temp;
        private int weatherCode;
        private String condition;

        public double getTemp() {return temp;}
        public int getWeatherCode() {return weatherCode;}
        public String getCondition() {return condition;}

    }


}
