package com.vapid_software.prodigy.models;

import java.util.List;

public class UserHandleContactModel {
    private List<ContactModel> data;

    public UserHandleContactModel(List<ContactModel> data) {
        this.data = data;
    }

    public List<ContactModel> getData() {
        return data;
    }
}
