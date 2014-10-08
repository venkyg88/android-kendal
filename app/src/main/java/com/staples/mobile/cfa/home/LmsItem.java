package com.staples.mobile.cfa.home;

import android.widget.ListView;

public class LmsItem {

    public String title;
    public String bannerUrl;
    public String identifier;
    public ListView productListView;

    LmsItem(String title, String bannerUrl, String identifier) {
        this.title = title;
        this.bannerUrl = bannerUrl;
        this.identifier = identifier;
    }
}
