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

    public Class fragmentClass;
    public Fragment fragment;

    // Top category
    public Drawable icon;
    public String title;
    public int childCount;
    public String path;

    // Constructors

    public DrawerItem(Type type) {
        this(type, null, 0, 0, null);
    }

    public DrawerItem(Type type, Context context, int iconId, int titleId) {
        this(type, context, iconId, titleId, null);
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

    public boolean instantiate(Context context) {
        if (fragment!=null) return(true);
        if (fragmentClass==null) return(false);

        fragment = Fragment.instantiate(context, fragmentClass.getName());
        Bundle args = new Bundle();
        args.putString("title", title);
        args.putString("path", path);
        fragment.setArguments(args);

        return(true);
    }
}
