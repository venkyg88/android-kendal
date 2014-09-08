package com.staples.drawertest;

import android.app.Fragment;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;

/**
* Created by pyhre001 on 8/14/14.
*/
public class DrawerItem {
    public static final String TAG = "DrawerItem";
    public enum Type {
        HEADER   (0, R.layout.drawer_header),
        FRAGMENT (1, R.layout.drawer_fragment),
        CATEGORY (2, R.layout.drawer_category);

        public int viewType;
        public int layoutId;

        Type(int viewType, int layoutId) {
            this.viewType = viewType;
            this.layoutId = layoutId;
        }
    }

    public static final int NTYPES = Type.values().length;

    // Base type
    public Type type;
    public String title;
    public Drawable icon;

    // Fragments & categories
    public Class fragmentClass;
    public Fragment fragment;

    // Top categories
    public int childCount;
    public String path;

    // Constructors

    public DrawerItem(Type type) {
        this(type, null, 0, 0, null);
    }

    public DrawerItem(Type type, Context context, int iconId, int titleId) {
        this(type, context, iconId, titleId, null);
    }

    public boolean isEnabled() {
        switch (type) {
            case HEADER:
                return(false);
            case CATEGORY:
                return(path!=null);
            default:
                return(true);
        }
    }

    public DrawerItem(Type type, Context context, int iconId, int titleId, Class<? extends Fragment> fragmentClass) {
        this.type = type;
        if (context!=null) {
            Resources resources = context.getResources();
            if (iconId!=0)
                icon =resources.getDrawable(iconId);
            if (titleId!=0)
                title = resources.getString(titleId);
        }
        this.fragmentClass = fragmentClass;
    }

    public DrawerItem(Type type, String title, int childCount, String path, Class<? extends Fragment> fragmentClass) {
        this.type = type;
        this.title = title;
        this.childCount = childCount;
        this.path = path;
        this.fragmentClass = fragmentClass;
    }

    // Fragment instantiation

    public Fragment instantiate(Context context) {
        if (fragment!=null) return(fragment);
        if (fragmentClass==null) return(null);

        fragment = Fragment.instantiate(context, fragmentClass.getName());
        Bundle args = new Bundle();
        args.putString("title", title);
        args.putString("path", path);
        fragment.setArguments(args);
        return(fragment);
    }
}
