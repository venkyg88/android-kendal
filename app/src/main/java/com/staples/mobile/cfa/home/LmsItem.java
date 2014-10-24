package com.staples.mobile.cfa.home;

import android.util.Log;
import android.widget.ListView;

public class LmsItem {

    public static final String TAG = "LmsItem";

    public String title;
    public String bannerUrl;
    public String identifier;
    public String size;
    public ListView productListView;

    LmsItem(String title, String bannerUrl, String identifier, String size) {

        Log.v(TAG, "LmsItem:LmsItem():"
                + " size[" + size + "]"
                + " title[" + title + "]"
                + " bannerUrl[" + bannerUrl + "]"
                + " identifier[" + identifier + "]"
                + " this[" + this + "]"
        );

        this.title = title;
        this.bannerUrl = bannerUrl;
        this.identifier = identifier;
        this.size = size;
    }
}
