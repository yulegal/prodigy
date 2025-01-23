package com.vapid_software.prodigy.models;

import com.vapid_software.prodigy.data.BookingExtraData;

public class BookingCreateModel {
    private String serviceId;
    private long bookDate;
    private String branchId;
    private BookingExtraData extra;

    public BookingCreateModel(String serviceId, long bookDate, String branchId, BookingExtraData extra) {
        this.serviceId = serviceId;
        this.extra = extra;
        this.branchId = branchId;
        this.bookDate = bookDate;
    }

    public BookingExtraData getExtra() {
        return extra;
    }

    public String getBranchId() {
        return branchId;
    }

    public String getServiceId() {
        return serviceId;
    }

    public long getBookDate() {
        return bookDate;
    }
}
