package com.vapid_software.prodigy.models;

public class AuthResponseModel {
    private String accessToken;
    private UserModel user;
    private String code;

    public String getAccessToken() {
        return accessToken;
    }

    public UserModel getUser() {
        return user;
    }

    public String getCode() {
        return code;
    }
}
