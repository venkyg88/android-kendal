package com.staples.mobile.cfa.bundle;

import com.staples.mobile.cfa.IdentifierType;
import com.staples.mobile.common.access.easyopen.model.browse.*;

import java.util.List;

public class BundleItem {
    public String title;
    public String identifier;
    public IdentifierType type;
    public String imageUrl;
    public float price;
    public String unit;
    public float customerRating;
    public int customerCount;

    public BundleItem(String title, String identifier) {
        this.title = title;
        this.identifier = identifier;
        type = IdentifierType.detect(identifier);
    }

    public String setImageUrl(List<Image> images) {
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

    public Float setPrice(List<Pricing> pricings) {
        if (pricings==null) return(null);
        for(Pricing pricing : pricings) {
            float finalPrice = pricing.getFinalPrice();
            if (finalPrice>0.0f) {
                price = finalPrice;
                unit = pricing.getUnitOfMeasure();
                return(price);
            }
        }
        return(null);
    }
}
