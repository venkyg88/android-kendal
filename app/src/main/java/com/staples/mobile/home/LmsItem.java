package com.staples.mobile.home;

import android.widget.ListView;

public class LmsItem {
    String title;
    String bannerUrl;
    String identifier;
    ListView list;

    LmsItem(String title, String bannerUrl, String identifier) {
        this.title = title;
        this.bannerUrl = bannerUrl;
        this.identifier = identifier;
    }
}
