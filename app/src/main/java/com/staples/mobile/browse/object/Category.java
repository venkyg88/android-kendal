package com.staples.mobile.browse.object;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by pyhre001 on 9/10/14.
 */
public class Category {
    private String catalogId;
    private String categoryUrl;
    private int childCount;
    @JsonProperty("description")
    private Description[] description1;
    @JsonProperty("Description")
    private Description[] description2;
    private String identifier;
    private boolean navigable;
    private boolean partialResults;
    private SubCategory[] subCategory;
    private FilterGroup filterGroup[];

    public String getCatalogId() {
        return catalogId;
    }

    public String getCategoryUrl() {
        return categoryUrl;
    }

    public int getChildCount() {
        return childCount;
    }

    public Description[] getDescription1() {
        return description1;
    }

    public Description[] getDescription2() {
        return description2;
    }

    public String getIdentifier() {
        return identifier;
    }

    public boolean isNavigable() {
        return navigable;
    }

    public boolean isPartialResults() {
        return partialResults;
    }

    public SubCategory[] getSubCategory() {
        return subCategory;
    }

    public FilterGroup[] getFilterGroup() {
        return filterGroup;
    }
}
