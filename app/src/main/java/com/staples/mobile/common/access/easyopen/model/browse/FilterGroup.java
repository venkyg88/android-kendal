package com.staples.mobile.common.access.easyopen.model.browse;

public class FilterGroup {
    private Filter[] filter;
    private String id;
    private String name;
    private int productsCount;

    public Filter[] getFilter() {
        return filter;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getProductsCount() {
        return productsCount;
    }
}
