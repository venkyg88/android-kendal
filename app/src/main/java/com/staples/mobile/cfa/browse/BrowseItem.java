package com.staples.mobile.cfa.browse;

import com.staples.mobile.cfa.R;

public class BrowseItem {
    private static final String TAG = "BrowseItem";

    public enum Type {
        STACK (0, R.layout.browse_stack),
        ITEM  (1, R.layout.browse_item);

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
    public String identifier;
}
