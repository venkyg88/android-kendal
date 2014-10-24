package com.staples.mobile.common.access.easyopen.model.search;

import java.util.List;

public class Image {
    String altText;
    String name;
    String promotionalIconAltText;
    String promotionalIconName;
    String promotionalIconUrl;
    List<ThumbnailImage> thumbnailImage;
    String url;

    public String getAltText() {
        return altText;
    }

    public String getName() {
        return name;
    }

    public String getPromotionalIconAltText() {
        return promotionalIconAltText;
    }

    public String getPromotionalIconName() {
        return promotionalIconName;
    }

    public String getPromotionalIconUrl() {
        return promotionalIconUrl;
    }

    public List<ThumbnailImage> getThumbnailImage() {
        return thumbnailImage;
    }

    public String getUrl() {
        return url;
    }
}
