package com.staples.mobile.common.access.easyopen.model.browse;

import java.util.List;

public class FilterGroup {
    List<Filter> filter;
    String id;
    String name;
    int productsCount;

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
