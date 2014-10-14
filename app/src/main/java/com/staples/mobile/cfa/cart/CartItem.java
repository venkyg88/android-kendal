/*
 * Copyright (c) 2014 Staples, Inc. All rights reserved.
 */

package com.staples.mobile.cfa.cart;

import com.staples.mobile.common.access.easyopen.model.cart.Image;
import com.staples.mobile.common.access.easyopen.model.cart.Pricing;
import com.staples.mobile.common.access.easyopen.model.cart.Product;
import com.staples.mobile.common.access.easyopen.model.cart.ThumbnailImage;

import java.util.List;

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
        List<Pricing> pricings = product.getPricing();
        if (pricings != null) {
            for (Pricing pricing : pricings) {
                //TODO: remove this conversion
                float price = 0.0f;
                if (pricing.getFinalPrice() != null) {
                    try { price = Float.parseFloat(pricing.getFinalPrice()); } catch(NumberFormatException e) {}
                }

               if (price > 0.0f) {
                    return pricing;
                }
            }
        }
        return null;
    }

    public float getFinalPrice() {
        //TODO: remove this conversion
        float price = 0.0f;
        if (getPricing().getFinalPrice() != null) {
            try { price = Float.parseFloat(getPricing().getFinalPrice()); } catch(NumberFormatException e) {}
        }
        return price;
    }

    public Image getImage() {
        List<Image> images = product.getImage();
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
        List<ThumbnailImage> images = product.getThumbnailImage();
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
