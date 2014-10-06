package com.staples.mobile.cfa.bundle;

import com.staples.mobile.common.access.easyopen.model.browse.*;

public class BundleItem {
    public String title;
    public String identifier;
    public String imageUrl;

    public BundleItem(String title, String identifier) {
        this.title = title;
        this.identifier = identifier;
    }

    public String setImageUrl(ThumbnailImage[] thumbs) {
        for(ThumbnailImage thumb : thumbs) {
            if (thumb.getUrl()!=null) {
                imageUrl = thumb.getUrl();
                return(imageUrl);
            }
        }
        return(null);
    }
}
