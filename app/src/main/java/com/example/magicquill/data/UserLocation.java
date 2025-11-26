package com.example.magicquill.data;

public class UserLocation {
    private double latitude;
    private double longitude;

    public UserLocation(double lat, double lon) {
        latitude = lat; longitude = lon;
    }

    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }

    public void setLatitude(double latitude) { this.latitude = latitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
}

