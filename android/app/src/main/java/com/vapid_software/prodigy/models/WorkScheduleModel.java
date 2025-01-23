package com.vapid_software.prodigy.models;

public class WorkScheduleModel {
    private long startTime;
    private long endTime;
    private String weekDay;
    private boolean allDay;

    public WorkScheduleModel(long startTime, long endTime, String weekDay, boolean allDay) {
        this.startTime = startTime;
        this.allDay = allDay;
        this.endTime = endTime;
        this.weekDay = weekDay;
    }

    public boolean isAllDay() {
        return allDay;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public String getWeekDay() {
        return weekDay;
    }
}
