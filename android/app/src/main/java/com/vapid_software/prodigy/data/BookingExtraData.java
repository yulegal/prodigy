package com.vapid_software.prodigy.data;

public class BookingExtraData {
    private int table;
    private int customSeats;

    public BookingExtraData(int table, int customSeats) {
        this.table = table;
        this.customSeats = customSeats;
    }

    public int getCustomSeats() {
        return customSeats;
    }

    public int getTable() {
        return table;
    }
}
