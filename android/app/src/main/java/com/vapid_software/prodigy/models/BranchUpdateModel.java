package com.vapid_software.prodigy.models;

import com.vapid_software.prodigy.data.ServiceExtraData;

import java.util.List;

public class BranchUpdateModel extends BranchCreateModel {
    private String id;

    public BranchUpdateModel(String serviceId, int averageSession, AddressModel address, List<String> userIds, String unit, List<WorkScheduleModel> workSchedule, ServiceExtraData extra, String id) {
        super(serviceId, averageSession, address, userIds, unit, workSchedule, extra);
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
