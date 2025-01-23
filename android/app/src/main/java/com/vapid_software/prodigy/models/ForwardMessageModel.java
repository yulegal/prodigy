package com.vapid_software.prodigy.models;

public class ForwardMessageModel {
    private String id;
    private String toId;

    public ForwardMessageModel(String id, String toId) {
        this.id = id;
        this.toId = toId;
    }
}
