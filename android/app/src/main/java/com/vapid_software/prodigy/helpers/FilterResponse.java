package com.vapid_software.prodigy.helpers;

import java.util.List;

public class FilterResponse<T> {
    private int page = 1;
    private int limit = 1000;
    private int total;
    private List<T> data;
    private int count;

    public int getCount() {
        return count;
    }

    public FilterResponse(List<T> data, int page, int limit, int total, int count) {
        this.data = data;
        this.page = page;
        this.limit = limit;
        this.total = total;
        this.count = count;
    }

    public int getPage() {
        return page;
    }

    public int getLimit() {
        return limit;
    }

    public List<T> getData() {
        return data;
    }

    public int getTotal() {
        return total;
    }
}
