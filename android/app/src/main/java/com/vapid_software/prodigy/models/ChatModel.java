package com.vapid_software.prodigy.models;

public class ChatModel {
    private String id;
    private UserModel user1;
    private UserModel user2;
    private long createdAt;
    private MessageModel message;

    public String getId() {
        return id;
    }

    public UserModel getUser1() {
        return user1;
    }

    public UserModel getUser2() {
        return user2;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public MessageModel getMessage() {
        return message;
    }
}
