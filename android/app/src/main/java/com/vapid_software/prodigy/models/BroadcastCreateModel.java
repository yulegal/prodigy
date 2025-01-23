package com.vapid_software.prodigy.models;

public class BroadcastCreateModel {
    private String message;
    private long date;
    private String action;
    private String branchId;

    public BroadcastCreateModel(String message, long date, String action, String branchId) {
        this.message = message;
        this.branchId = branchId;
        this.date = date;
        this.action = action;
    }

    public String getBranchId() {
        return branchId;
    }

    public String getMessage() {
        return message;
    }

    public long getDate() {
        return date;
    }

    public String getAction() {
        return action;
    }
}
