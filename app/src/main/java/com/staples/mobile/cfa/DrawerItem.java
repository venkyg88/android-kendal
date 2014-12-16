package com.staples.mobile.cfa;

import android.app.Fragment;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

public class DrawerItem {
    private static final String TAG = "DrawerItem";

    public enum Type {
        FRAGMENT  (0, R.layout.drawer_fragment),
        ACCOUNT   (1, R.layout.drawer_account),
        PROFILE   (2, R.layout.drawer_fragment);

        public int viewType;
        public int layoutId;

        Type(int viewType, int layoutId) {
            this.viewType = viewType;
            this.layoutId = layoutId;
        }
    }

    public static final int NTYPES = Type.values().length;

    // Generic info
    public Type type;
    public String title;
    public Drawable icon;
    public boolean enabled;

    // For fragments
    public Class fragmentClass;
    public Fragment fragment;

    // Constructor

    public DrawerItem(Type type, Context context, int iconId, int titleId, Class<? extends Fragment> fragmentClass) {
        this(type, context, iconId, titleId, fragmentClass, true);
    }

    public DrawerItem(Type type, Context context, int iconId, int titleId, Class<? extends Fragment> fragmentClass, boolean enabled) {
        this.type = type;
        if (context!=null) {
            Resources resources = context.getResources();
            if (iconId!=0)
                icon =resources.getDrawable(iconId);
            if (titleId!=0)
                title = resources.getString(titleId);
        }
        this.fragmentClass = fragmentClass;
        this.enabled = enabled;
    }

    // Fragment instantiation

    public Fragment instantiate(Context context) {
        if (fragment!=null) return(fragment);
        if (fragmentClass==null) return(null);

        fragment = Fragment.instantiate(context, fragmentClass.getName());
        Bundle args = new Bundle();
        if (title!=null) args.putString("title", title);
        fragment.setArguments(args);
        return(fragment);
    }
}
