/*
 * Copyright (c) 2014 Staples, Inc. All rights reserved.
 */

package com.staples.mobile.cfa.cart;

import android.support.annotation.NonNull;

import com.staples.mobile.common.access.easyopen.model.cart.Image;
import com.staples.mobile.common.access.easyopen.model.cart.Pricing;
import com.staples.mobile.common.access.easyopen.model.cart.Product;
import com.staples.mobile.common.access.easyopen.model.cart.ThumbnailImage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class CartItem {
    public static final String TAG = "CartItem";

    // Generic info
    private Product product;

    // Constructor
    public CartItem(Product product) {
        this.product = product;
    }

    // TODO: remove this temporary conversion Constructor
    public CartItem(com.staples.mobile.common.access.easyopen.model.browse.Product product) {
        this.product = new Product();
        this.product.setQuantity("1");
        this.product.setProductName(product.getProductName());
        List<Pricing> pricings = new ArrayList<Pricing>();
        for (com.staples.mobile.common.access.easyopen.model.browse.Pricing p : product.getPricing()) {
            Pricing pricing = new Pricing();
            pricing.setFinalPrice(""+p.getFinalPrice());
            pricing.setUnitOfMeasure(p.getUnitOfMeasure());
            pricings.add(pricing);
        }
        this.product.setPricing(pricings);
        List<Image> images = new ArrayList<Image>();
        for (com.staples.mobile.common.access.easyopen.model.browse.Image img : product.getImage()) {
            Image image = new Image();
            image.setUrl(""+img.getUrl());
            images.add(image);
        }
        this.product.setImage(images);
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
        Pricing pricing = getPricing();
        if (pricing != null && pricing.getFinalPrice() != null) {
            try { price = Float.parseFloat(getPricing().getFinalPrice()); } catch(NumberFormatException e) {}
        }
        return price;
    }

    public String getPriceUnitOfMeasure() {
        Pricing pricing = getPricing();
        if (pricing != null) {
            return pricing.getUnitOfMeasure();
        }
        return null;
    }

    public String getImageUrl() {
        List<Image> images = product.getImage();
        if (images != null) {
            for(Image image : images) {
                if (image.getUrl() != null) {
                    return image.getUrl();
                }
            }
        }
        return null;
    }

    public String getThumbnailImageUrl() {
        List<ThumbnailImage> images = product.getThumbnailImage();
        if (images != null) {
            for(ThumbnailImage image : images) {
                if (image.getUrl() != null) {
                    return image.getUrl();
                }
            }
        }
        return null;
    }


    public int getQuantity() {
        //TODO: remove this conversion
        int quantity = 0;
        if (product.getQuantity() != null) {
            try { quantity = Integer.parseInt(product.getQuantity()); } catch(NumberFormatException e) {}
        }
        return quantity;
    }

    public void setQuantity(int quantity) {
        //TODO: remove this conversion
        String strQty = String.valueOf(quantity);
        product.setQuantity(strQty);
    }
}
