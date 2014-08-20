package com.staples.drawertest.browse;

import android.graphics.drawable.Drawable;

/**
 * Created by pyhre001 on 8/20/14.
 */
public class CategoryItem {
    public Drawable icon;
    public String title;
    public int childCount;
    public String path;

    public CategoryItem(String title, int childCount, String path) {
        this.title = title;
        this.childCount = childCount;
        this.path = path;
    }
}
