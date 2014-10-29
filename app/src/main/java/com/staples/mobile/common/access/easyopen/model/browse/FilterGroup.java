package com.staples.mobile.common.access.easyopen.model.browse;

import java.util.List;

public class FilterGroup {
    private List<Filter> filter;
    private String id;
    private String name;
    private int productsCount;

    public List<Filter> getFilter() {
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
