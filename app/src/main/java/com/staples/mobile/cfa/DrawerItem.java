package com.staples.mobile.cfa;

import android.app.Fragment;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

public class DrawerItem {
    private static final String TAG = DrawerItem.class.getSimpleName();

    // Generic info
    public int id;
    public String title;
    public String extra;
    public Drawable icon;
    public boolean enabled;

    // For fragments
    public Class fragmentClass;
    public Fragment fragment;

    // Constructor

    public DrawerItem(Context context, int iconId, int titleId, Class<? extends Fragment> fragmentClass) {
        this(context, iconId, titleId, fragmentClass, true);
    }

    public DrawerItem(Context context, int iconId, int titleId, Class<? extends Fragment> fragmentClass, boolean enabled) {
        id = titleId;
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
