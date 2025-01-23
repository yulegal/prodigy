package com.vapid_software.prodigy.models;

import java.util.List;

public class MessageModel {
    private String id;
    private String body;
    private long createdAt;
    private ChatModel chat;
    private boolean isRead;
    private UserModel forwardedFrom;
    private List<String> addons;
    private MessageModel parent;
    private UserModel from;
    private UserModel to;
    private boolean edited;

    public boolean isEdited() {
        return edited;
    }

    public UserModel getForwardedFrom() {
        return forwardedFrom;
    }

    public String getId() {
        return id;
    }

    public String getBody() {
        return body;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public ChatModel getChat() {
        return chat;
    }

    public boolean isRead() {
        return isRead;
    }

    public List<String> getAddons() {
        return addons;
    }

    public MessageModel getParent() {
        return parent;
    }

    public UserModel getFrom() {
        return from;
    }

    public UserModel getTo() {
        return to;
    }

    public void setRead(boolean read) {
        isRead = read;
    }
}
