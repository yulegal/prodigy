package com.vapid_software.prodigy.models;

import com.vapid_software.prodigy.data.BookingExtraData;

public class BookingModel {
    private String id;
    private long createdAt;
    private ServiceModel service;
    private BranchModel branch;
    private String status;
    private long bookDate;
    private BookingExtraData extra;

    public BookingExtraData getExtra() {
        return extra;
    }

    public String getId() {
        return id;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public ServiceModel getService() {
        return service;
    }

    public BranchModel getBranch() {
        return branch;
    }

    public String getStatus() {
        return status;
    }

    public long getBookDate() {
        return bookDate;
    }
}
