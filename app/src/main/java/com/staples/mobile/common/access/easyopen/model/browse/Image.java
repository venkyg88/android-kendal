package com.staples.mobile.common.access.easyopen.model.browse;

public class Image {
    private String altText;
    private String name;
    private String promotionalIconAltText;
    private String promotionalIconName;
    private String promotionalIconUrl;
    private ThumbnailImage[] thumbnailImage;
    private String url;

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

    public ThumbnailImage[] getThumbnailImage() {
        return thumbnailImage;
    }

    public String getUrl() {
        return url;
    }
}
