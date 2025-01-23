package com.vapid_software.prodigy.helpers;

public class FilterItem {
    private String name;
    private String value;
    private String comparison;

    public final static String COMPARISON_OP_EQ = "eq";
    public final static String COMPARISON_OP_NEQ = "neq";
    public final static String COMPARISON_OP_LIKE = "like";
    public final static String COMPARISON_OP_GREATER_THAN = "gt";
    public final static String COMPARISON_OP_GREATER_THAN_EQ = "gte";
    public final static String COMPARISON_OP_LESS_THAN = "lt";
    public final static String COMPARISON_OP_LESS_THAN_EQ = "lte";
    public final static String COMPARISON_OP_ILIKE = "ilike";

    public FilterItem(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public FilterItem(String name, String value, String comparison) {
        this(name, value);
        this.comparison = comparison;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public String getComparison() {
        return comparison;
    }
}
