package com.vapid_software.prodigy.models;

public class ServiceModel extends ServiceBaseModel {
    private String id;
    private String name;
    private long createdAt;
    private UserModel user;
    private CategoryModel category;
    private boolean blocked;
    private String icon;
    private long trialEndDate;
    private long paymentEndDate;
    private boolean addedToFavorites;

    public boolean isAddedToFavorites() {
        return addedToFavorites;
    }

    public void setAddedToFavorites(boolean addedToFavorites) {
        this.addedToFavorites = addedToFavorites;
    }

    public long getTrialEndDate() {
        return trialEndDate;
    }

    public long getPaymentEndDate() {
        return paymentEndDate;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public UserModel getUser() {
        return user;
    }

    public CategoryModel getCategory() {
        return category;
    }

    public boolean isBlocked() {
        return blocked;
    }

    public String getIcon() {
        return icon;
    }
}
