package com.staples.mobile;

import android.app.Fragment;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

public class DrawerItem {
    public static final String TAG = "DrawerItem";
    public enum Type {
        ACCOUNT   (0, R.layout.drawer_account),
        FRAGMENT (1, R.layout.drawer_fragment);

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


    // Fragment instantiation

    public Fragment instantiate(Context context) {
        if (fragment!=null) return(fragment);
        if (fragmentClass==null) return(null);

        fragment = Fragment.instantiate(context, fragmentClass.getName());
        Bundle args = new Bundle();
        args.putString("title", title);
        fragment.setArguments(args);
        return(fragment);
    }
}
