package com.vapid_software.prodigy.models;

public class RatingModel {
    private String id;
    private int rating;

    public RatingModel(String id, int rating) {
        this.id = id;
        this.rating = rating;
    }

    public String getId() {
        return id;
    }

    public int getRating() {
        return rating;
    }
}
