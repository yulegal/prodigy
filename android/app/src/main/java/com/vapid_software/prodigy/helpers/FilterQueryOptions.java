package com.vapid_software.prodigy.helpers;

import java.util.ArrayList;
import java.util.List;

public class FilterQueryOptions {
    private int page = 1;
    private int limit = 1000;
    private List<FilterItem> filter;

    public FilterQueryOptions(int page, int limit, List<FilterItem> filter) {
        this(filter);
        this.page = page;
        this.limit = limit;
    }

    public FilterQueryOptions(List<FilterItem> items) {
        this.filter = items == null ? new ArrayList<>() : items;
    }

    public int getPage() {
        return page;
    }

    public int getLimit() {
        return limit;
    }

    public List<FilterItem> getFilter() {
        return filter;
    }
}
