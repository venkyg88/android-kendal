package com.staples.mobile.browse.object;

/**
 * Created by pyhre001 on 9/10/14.
 */
public class Category {
    private String categoryUrl;
    private Description[] description;
    private int childCount;
    private SubCategory[] subCategory;
    private FilterGroup filterGroup[];

    public String getCategoryUrl() {
        return categoryUrl;
    }

    public Description[] getDescription() {
        return description;
    }

    public int getChildCount() {
        return childCount;
    }

    public SubCategory[] getSubCategory() {
        return subCategory;
    }

    public FilterGroup[] getFilterGroup() {
        return filterGroup;
    }
}
