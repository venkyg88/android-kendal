package com.staples.mobile.browse;

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
    public String path;
    public int childCount;

    public CategoryItem(String title, String path, int childCount) {
        this.title = title;
        this.path = path;
        this.childCount = childCount;
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
