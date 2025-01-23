package com.vapid_software.prodigy.models;

public class AddressModel {
    private String address;
    private String url;

    public AddressModel(String address, String url) {
        this.address = address;
        this.url = url;
    }

    public String getAddress() {
        return address;
    }

    public String getUrl() {
        return url;
    }
}
