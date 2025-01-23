package com.vapid_software.prodigy.models;

public class ContactModel {
    private String name;
    private String number;

    public String getName() {
        return name;
    }

    public String getNumber() {
        return number;
    }

    public ContactModel(String name, String number) {
        this.name = name;
        this.number = number;
    }
}
