package com.staples.mobile.cfa.browse;

import com.staples.mobile.cfa.R;

public class BrowseItem {
    private static final String TAG = BrowseItem.class.getSimpleName();

    public enum Type {
        STACK    (0, R.layout.browse_stack),
        ACTIVE   (1, R.layout.browse_active),
        ITEM     (2, R.layout.browse_item),
        SELECTED (3, R.layout.browse_selected);

        public final int viewType;
        public final int layoutId;

        Type(int viewType, int layoutId) {
            this.viewType = viewType;
            this.layoutId = layoutId;
        }
    }

    public Type type;
    public String title;
    public String identifier;

    public BrowseItem(Type type, String title, String identifier) {
        this.type = type;
        this.title = title;
        this.identifier = identifier;
    }
}
