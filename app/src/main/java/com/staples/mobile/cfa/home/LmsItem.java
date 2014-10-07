package com.staples.mobile.cfa.home;

import android.widget.ListView;

public class LmsItem {
    String title;
    String bannerUrl;
    String identifier;
    ListView listView;

    LmsItem(String title, String bannerUrl, String identifier) {
        this.title = title;
        this.bannerUrl = bannerUrl;
        this.identifier = identifier;
    }
}
