package app.staples.mobile.cfa;

import android.app.Fragment;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

public class DrawerItem {
    private static final String TAG = DrawerItem.class.getSimpleName();

    // Unique (and arbitrary) tags for marking back stack
    public static final String HOME     = "001";
    public static final String FEED     = "002";
    public static final String BROWSE   = "003";
    public static final String STORE    = "004";
    public static final String WEEKLY   = "005";
    public static final String ACCOUNT  = "006";
    public static final String REWARDS  = "007";
    public static final String ORDERS   = "008";
    public static final String PROFILE  = "009";
    public static final String NOTIFY   = "010";
    public static final String TERMS    = "011";
    public static final String ABOUT    = "012";

    // Non-drawer items
    public static final String LOGIN    = "013";
    public static final String PASSWORD = "014";
    public static final String BUNDLE   = "015";
    public static final String SEARCH   = "016";
    public static final String SKUSET   = "017";
    public static final String SKU      = "018";
    public static final String ADDRESS  = "019";
    public static final String CARD     = "020";
    public static final String CART     = "021";
    public static final String CONFIRM  = "023";
    public static final String LINK     = "024";
    public static final String SALES    = "025";
    public static final String ORDERDETAIL = "026";
    public static final String WEEKLYDETAIL = "027";
    public static final String REG_CHECKOUT = "028";
    public static final String GUEST_CHECKOUT = "029";

    // Generic info
    public String tag;
    public String title;
    public String extra;
    public Drawable icon;
    public boolean enabled;

    // For fragments
    public Class fragmentClass;
    public Fragment fragment;

    // Constructor

    public DrawerItem(Context context, String tag, int iconId, int titleId, Class<? extends Fragment> fragmentClass) {
        this(context, tag, iconId, titleId, fragmentClass, true);
    }

    public DrawerItem(Context context, String tag, int iconId, int titleId, Class<? extends Fragment> fragmentClass, boolean enabled) {
        this.tag = tag;
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
