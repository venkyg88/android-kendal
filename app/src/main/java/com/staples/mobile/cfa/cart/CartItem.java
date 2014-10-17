/*
 * Copyright (c) 2014 Staples, Inc. All rights reserved.
 */

package com.staples.mobile.cfa.cart;

import com.staples.mobile.common.access.easyopen.model.cart.Image;
import com.staples.mobile.common.access.easyopen.model.cart.Pricing;
import com.staples.mobile.common.access.easyopen.model.cart.Product;

import java.util.List;

public class CartItem {
    public static final String TAG = "CartItem";

    private Product product;
    private int proposedQty;

    // Constructor
    public CartItem(Product product) {
        this.product = product;
    }

//    // TODO: remove this temporary conversion Constructor
//    public CartItem(com.staples.mobile.common.access.easyopen.model.browse.Product product) {
//        this.product = new Product();
//        this.product.setQuantity(1);
//        this.product.setProductName(product.getProductName());
//        List<Pricing> pricings = new ArrayList<Pricing>();
//        for (com.staples.mobile.common.access.easyopen.model.browse.Pricing p : product.getPricing()) {
//            Pricing pricing = new Pricing();
//            pricing.setFinalPrice(p.getFinalPrice());
//            pricing.setUnitOfMeasure(p.getUnitOfMeasure());
//            pricings.add(pricing);
//        }
//        this.product.setPricing(pricings);
//        List<Image> images = new ArrayList<Image>();
//        for (com.staples.mobile.common.access.easyopen.model.browse.Image img : product.getImage()) {
//            Image image = new Image();
//            image.setUrl(""+img.getUrl());
//            images.add(image);
//        }
//        this.product.setImage(images);
//    }

    public String getDescription() {
        return product.getProductName();
    }

    public String getOrderItemId() {
        return product.getOrderItemId();
    }

    public String getSku() {
        return product.getSku();
    }

    public String getPartNumber() {
        return product.getManufacturerPartNumber();
    }

    public Pricing getPricing() {
        List<Pricing> pricings = product.getPricing();
        if (pricings != null) {
            for (Pricing pricing : pricings) {
                if (pricing.getFinalPrice() > 0.0f) {
                    return pricing;
                }
            }
        }
        return null;
    }

    public float getFinalPrice() {
        Pricing pricing = getPricing();
        if (pricing != null) {
            return pricing.getFinalPrice();
        }
        return 0.0f;
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
        List<Image> images = product.getThumbnailImage();
        if (images != null) {
            for(Image image : images) {
                if (image.getUrl() != null) {
                    return image.getUrl();
                }
            }
        }
        return null;
    }


    public int getQuantity() {
        return product.getQuantity();
    }

    public void setQuantity(int quantity) {
        product.setQuantity(quantity);
    }

    public int getProposedQty() {
        return proposedQty;
    }

    public void setProposedQty(int proposedQty) {
        this.proposedQty = proposedQty;
    }
}
