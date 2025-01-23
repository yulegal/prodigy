package com.vapid_software.prodigy.models;

import com.vapid_software.prodigy.data.ServiceExtraData;

import java.util.List;

public abstract class ServiceBaseModel {
    private int averageSession;
    private String unit;
    private List<WorkScheduleModel> workSchedule;
    private AddressModel address;
    private int rating;
    private ServiceExtraData extra;

    public void setRating(int rating) {
        this.rating = rating;
    }

    public ServiceExtraData getExtra() {
        return extra;
    }

    public AddressModel getAddress() {
        return address;
    }

    public int getRating() {
        return rating;
    }

    public int getAverageSession() {
        return averageSession;
    }

    public String getUnit() {
        return unit;
    }

    public List<WorkScheduleModel> getWorkSchedule() {
        return workSchedule;
    }
}
