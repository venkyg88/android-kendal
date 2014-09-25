package com.staples.mobile.cfa.browse;

import android.app.Fragment;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import com.staples.mobile.cfa.bundle.BundleFragment;

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
        String fragmentClass = CategoryFragment.class.getName();

        // Check if the path refers to a class => bundle
        int i = path.indexOf("/category/identifier/");
        if (i >= 0) {
            i += "/category/identifier/".length();
            int j = path.indexOf('?', i);
            if (j <= 0) j = path.length();
            String identifier = path.substring(i, j);
            if (identifier.startsWith("CL"))
                fragmentClass = BundleFragment.class.getName();
        }

        Fragment fragment = Fragment.instantiate(context, fragmentClass);
        Bundle args = new Bundle();
        args.putString("title", title);
        args.putString("path", path);
        fragment.setArguments(args);
        return (fragment);
    }
}
