package com.staples.mobile.cfa.bundle;

import android.graphics.drawable.Drawable;

public class BundleItem {
    public Drawable icon;
    public String title;
    public String identifier;

    public BundleItem(String title, String identifier) {
        this.title = title;
        this.identifier = identifier;
    }
}
