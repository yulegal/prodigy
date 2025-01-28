package com.vapid_software.prodigy.models;

public class AddressModel {
    private String address;
    private String url;
    private Location location;

    public static class Location {
        private double latitude;
        private double longitude;

        public double getLatitude() {
            return latitude;
        }

        public double getLongitude() {
            return longitude;
        }

        public void setLatitude(double latitude) {
            this.latitude = latitude;
        }

        public void setLongitude(double longitude) {
            this.longitude = longitude;
        }
    }

    public Location getLocation() {
        return location;
    }

    public AddressModel(String address, String url, Location location) {
        this.address = address;
        this.url = url;
        this.location = location;
    }

    public String getAddress() {
        return address;
    }

    public String getUrl() {
        return url;
    }
}
