package com.vapid_software.prodigy.models;

public class NotificationModel {
    private String id;
    private String title;
    private String body;
    private boolean isRead;
    private long createdAt;
    private UserModel user;
    private String type;

    public void setRead(boolean read) {
        isRead = read;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getBody() {
        return body;
    }

    public boolean isRead() {
        return isRead;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public UserModel getUser() {
        return user;
    }

    public String getType() {
        return type;
    }
}
