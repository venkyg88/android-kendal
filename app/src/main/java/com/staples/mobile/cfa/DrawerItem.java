package com.staples.mobile.cfa;

import android.app.Fragment;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import com.staples.mobile.R;

public class DrawerItem {
    private static final String TAG = "DrawerItem";

    public enum Type {
        BROWSE    (0, R.layout.drawer_fragment), // Browse button in menu
        FRAGMENT  (1, R.layout.drawer_fragment), // Action fragment in menu
        ACCOUNT   (2, R.layout.drawer_account),  // Account fragment in menu
        BACKTOTOP (3, R.layout.drawer_fragment), // Back button in browse
        STACK     (4, R.layout.drawer_stack),    // Breadcrumb in browse
        CATEGORY  (5, R.layout.drawer_category), // Item in browse
        PROFILE   (6, R.layout.drawer_fragment);

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

    // For fragments
    public Class fragmentClass;
    public Fragment fragment;

    // For browse categories
    public String path;
    public String identifier;

    // Constructor

    public DrawerItem(Type type, Context context, int iconId, int titleId) {
        this(type, context, iconId, titleId, null);
    }

    public DrawerItem(Type type, Context context, int iconId, int titleId, Class<? extends BaseFragment> fragmentClass) {
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
        if (title!=null) args.putString("title", title);
        if (path!=null) args.putString("path", path);
        if (identifier!=null) args.putString("identifier", identifier);
        fragment.setArguments(args);
        return(fragment);
    }

    public String getBundleIdentifier() {
        if (type!=Type.CATEGORY || path==null) return(null);

        int i = path.indexOf("/category/identifier/");
        if (i<0) return(null);
        i += "/category/identifier/".length();
        int j = path.indexOf('?', i);
        if (j <= 0) j = path.length();
        String identifier = path.substring(i, j);
        if (identifier.startsWith("CL") ||
            identifier.startsWith("BI")) return(identifier);
        return(null);
    }
}
