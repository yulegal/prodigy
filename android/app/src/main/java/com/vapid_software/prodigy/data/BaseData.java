package com.vapid_software.prodigy.data;

public class BaseData {
    private String id;
    private String name;

    public BaseData(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
