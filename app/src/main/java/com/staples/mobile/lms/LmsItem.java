package com.staples.mobile.lms;

import android.widget.ListView;

class LmsItem {
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
