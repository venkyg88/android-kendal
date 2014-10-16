package com.staples.mobile.common.access.easyopen.model.browse;

import java.util.List;

public class SubCategory {
    String catalogId;
    String categoryUrl;
    int childCount;
    List<Description> description;
    String identifier;
    boolean navigable;

    public String getCatalogId() {
        return catalogId;
    }

    public String getCategoryUrl() {
        return categoryUrl;
    }

    public int getChildCount() {
        return childCount;
    }

    public List<Description> getDescription() {
        return description;
    }

    public String getIdentifier() {
        return identifier;
    }

    public boolean isNavigable() {
        return navigable;
    }
}
