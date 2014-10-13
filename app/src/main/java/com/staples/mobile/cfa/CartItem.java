/*
 * Copyright (c) 2014 Staples, Inc. All rights reserved.
 */

package com.staples.mobile.cfa;

import com.staples.mobile.common.access.easyopen.model.browse.Pricing;
import com.staples.mobile.common.access.easyopen.model.browse.Product;
import com.staples.mobile.common.access.easyopen.model.browse.ThumbnailImage;

public class CartItem {
    public static final String TAG = "CartItem";

    // Generic info
    private Product product;
    private int quantity;


    // Constructor
    public CartItem(Product product, int quantity) {
        this.product = product;
        this.quantity = quantity;
    }


    public String getDescription() {
        return product.getProductName();
    }

    public Pricing getPricing() {
        Pricing[] pricings = product.getPricing();
        if (pricings != null) {
            for (Pricing pricing : pricings) {
                if (pricing.getFinalPrice() > 0.0f) {
                    return pricing;
                }
            }
        }
        //List<Pricing> pricings = product.getPricing();
//        if (pricings != null && pricings.size() > 0) {
//            return pricings.get(0);
//        }
        return null;
    }

    public ThumbnailImage getThumbnailImage() {
        ThumbnailImage[] images = product.getThumbnailImage();
        if (images != null) {
            for(ThumbnailImage image : images) {
                if (image.getUrl() != null) {
                    return image;
                }
            }
        }
//        List<ThumbnailImage> images = product.getThumbnailImage();
//        if (images != null && images.size() > 0) {
//            return images.get(0);
//        }
        return null;
    }


    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
