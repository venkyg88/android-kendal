package com.staples.mobile.easyopen;

/**
 * Created by pyhre001 on 9/10/14.
 */
public class SubCategory {
    private String catalogId;
    private String categoryUrl;
    private int childCount;
    private Description[] description;
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

    public Description[] getDescription() {
        return description;
    }

    public String getIdentifier() {
        return identifier;
    }

    public boolean isNavigable() {
        return navigable;
    }
}
