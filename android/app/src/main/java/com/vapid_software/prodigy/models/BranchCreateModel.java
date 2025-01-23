package com.vapid_software.prodigy.models;

import com.vapid_software.prodigy.data.ServiceExtraData;

import java.util.List;

public class BranchCreateModel {
    private String serviceId;
    private int averageSession;
    private AddressModel address;
    private List<String> userIds;
    private String unit;
    private List<WorkScheduleModel> workSchedule;
    private ServiceExtraData extra;

    public BranchCreateModel(String serviceId, int averageSession, AddressModel address, List<String> userIds, String unit, List<WorkScheduleModel> workSchedule, ServiceExtraData extra) {
        this.serviceId = serviceId;
        this.extra = extra;
        this.averageSession = averageSession;
        this.address = address;
        this.userIds = userIds;
        this.unit = unit;
        this.workSchedule = workSchedule;
    }

    public ServiceExtraData getExtra() {
        return extra;
    }

    public String getServiceId() {
        return serviceId;
    }

    public int getAverageSession() {
        return averageSession;
    }

    public AddressModel getAddress() {
        return address;
    }

    public List<String> getUserIds() {
        return userIds;
    }

    public String getUnit() {
        return unit;
    }

    public List<WorkScheduleModel> getWorkSchedule() {
        return workSchedule;
    }
}
