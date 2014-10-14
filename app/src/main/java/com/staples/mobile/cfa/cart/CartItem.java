/*
 * Copyright (c) 2014 Staples, Inc. All rights reserved.
 */

package com.staples.mobile.cfa.cart;

import com.staples.mobile.common.access.easyopen.model.browse.Image;
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
        return null;
    }

    public Image getImage() {
        Image[] images = product.getImage();
        if (images != null) {
            for(Image image : images) {
                if (image.getUrl() != null) {
                    return image;
                }
            }
        }
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
        return null;
    }


    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
