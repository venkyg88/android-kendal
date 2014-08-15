package com.staples.drawertest;

import android.app.Fragment;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;

/**
* Created by pyhre001 on 8/14/14.
*/
public class DrawerItem {
    public enum Type {
        HEADER   (0, R.layout.drawer_header),
        FRAGMENT (1, R.layout.drawer_fragment),
        SEARCH   (2, R.layout.drawer_search);

        public int viewType;
        public int layoutId;

        Type(int viewType, int layoutId) {
            this.viewType = viewType;
            this.layoutId = layoutId;
        }
    }

    public static final int NTYPES = Type.values().length;

    public Type type;

    public String title;
    public Drawable icon;

    public Class fragmentClass;
    public Fragment fragment;

    public DrawerItem(Type type) {
        this(type, null, 0, 0, null);
    }

    public DrawerItem(Type type, Context context, int iconId, int titleId) {
        this(type, context, iconId, titleId, null);
    }

    public DrawerItem(Type type, Context context, int iconId, int titleId, Class fragmentClass) {
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

    public DrawerItem(Type type, String title, Fragment fragment) {
        this.type = type;
        this.title = title;
        if (fragment!=null)
            this.fragmentClass = fragment.getClass();
        this.fragment = fragment;
    }
}
