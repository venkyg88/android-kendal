package com.staples.mobile.object;

/**
 * Created by pyhre001 on 9/10/14.
 */
public class SubCategory {
    private String categoryUrl;
    private Description[] description;
    private int childCount;

    public String getCategoryUrl() {
        return categoryUrl;
    }

    public Description[] getDescription() {
        return description;
    }

    public int getChildCount() {
        return childCount;
    }
}
