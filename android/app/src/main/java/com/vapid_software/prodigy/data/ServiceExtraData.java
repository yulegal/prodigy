package com.vapid_software.prodigy.data;

import java.util.List;

public class ServiceExtraData {
    private List<Integer> tables;

    public ServiceExtraData(List<Integer> tables) {
        this.tables = tables;
    }

    public List<Integer> getTables() {
        return tables;
    }
}
