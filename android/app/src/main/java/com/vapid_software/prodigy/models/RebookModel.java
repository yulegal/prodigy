package com.vapid_software.prodigy.models;

import com.vapid_software.prodigy.data.BookingExtraData;

public class RebookModel extends BookingCreateModel {
    private String id;

    public RebookModel(String serviceId, long bookDate, String branchId, BookingExtraData extra, String id) {
        super(serviceId, bookDate, branchId, extra);
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
