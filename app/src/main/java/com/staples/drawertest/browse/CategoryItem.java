package com.staples.drawertest.browse;

import android.app.Fragment;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

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

    // Fragment instantiation

    public Fragment instantiate(Context context) {
        Fragment fragment = Fragment.instantiate(context, CategoryFragment.class.getName());
        Bundle args = new Bundle();
        args.putString("title", title);
        args.putString("path", path);
        fragment.setArguments(args);
        return(fragment);
    }

}
