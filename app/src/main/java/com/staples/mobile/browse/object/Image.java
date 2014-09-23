package com.staples.mobile.browse.object;

public class Image {
    private String altText;
    private String name;
    private ThumbnailImage[] thumbnailImage;
    private String url;

    public String getAltText() {
        return altText;
    }

    public String getName() {
        return name;
    }

    public ThumbnailImage[] getThumbnailImage() {
        return thumbnailImage;
    }

    public String getUrl() {
        return url;
    }
}
