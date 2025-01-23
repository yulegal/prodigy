package com.vapid_software.prodigy.models;

import java.util.List;

public class BranchModel extends ServiceBaseModel {
    private String id;
    private long createdAt;
    private ServiceModel service;
    private List<UserModel> users;

    public String getId() {
        return id;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public ServiceModel getService() {
        return service;
    }

    public List<UserModel> getUsers() {
        return users;
    }
}
