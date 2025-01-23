package com.vapid_software.prodigy.models;

import java.util.Map;

public class CategoryModel {
    private String id;
    private Map<String, String> name;
    private long createdAt;
    private String icon;
    private String type;

    public String getType() {
        return type;
    }

    public String getId() {
        return id;
    }

    public Map<String, String> getName() {
        return name;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public String getIcon() {
        return icon;
    }
}
