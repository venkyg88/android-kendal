package com.staples.mobile.common.access.easyopen.model.browse;

import java.util.List;

public class SubCategory {
    private String catalogId;
    private String categoryUrl;
    private int childCount;
    private List<Description> description;
    private String identifier;
    private boolean navigable;

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
