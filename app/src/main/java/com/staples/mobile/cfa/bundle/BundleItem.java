package com.staples.mobile.cfa.bundle;

import com.staples.mobile.common.access.easyopen.model.browse.*;

public class BundleItem {
    public String title;
    public String identifier;
    public String imageUrl;
    public float price;
    public float customerRating;
    public int customerCount;

    public BundleItem(String title, String identifier) {
        this.title = title;
        this.identifier = identifier;
    }

    public String setImageUrl(Image[] images) {
        if (images==null) return(null);
        for(Image image : images) {
            String url = image.getUrl();
            if (url!=null) {
                imageUrl = url;
                return(imageUrl);
            }
        }
        return(null);
    }

    public String setImageUrl(ThumbnailImage[] thumbs) {
        if (thumbs==null) return(null);
        for(ThumbnailImage thumb : thumbs) {
            String url = thumb.getUrl();
            if (url!=null) {
                imageUrl = url;
                return(imageUrl);
            }
        }
        return(null);
    }

    public Float setPrice(Pricing[] pricings) {
        if (pricings==null) return(null);
        for(Pricing pricing : pricings) {
            float finalPrice = pricing.getFinalPrice();
            if (finalPrice>0.0f) {
                price = finalPrice;
                return(price);
            }
        }
        return(null);
    }
}
