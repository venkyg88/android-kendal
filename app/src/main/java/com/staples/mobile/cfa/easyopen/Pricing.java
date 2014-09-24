package com.staples.mobile.cfa.easyopen;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by pyhre001 on 9/22/14.
 */
public class Pricing {
    @JsonProperty("BuyMoreSaveMoreDetail")
    private BuyMoreSaveMoreDetail[] buyMoreSaveMoreDetail;
    private String buyMoreSaveMoreImage;
    private boolean displayRegularPricing;
    private boolean displayWasPricing;
    private Product[] freeItem;
    private float finalPrice;
    private float freeItemTotalSavings;
    private float listPrice;
    private String unitOfMeasure;

    public BuyMoreSaveMoreDetail[] getBuyMoreSaveMoreDetail() {
        return buyMoreSaveMoreDetail;
    }

    public String getBuyMoreSaveMoreImage() {
        return buyMoreSaveMoreImage;
    }

    public boolean isDisplayRegularPricing() {
        return displayRegularPricing;
    }

    public boolean isDisplayWasPricing() {
        return displayWasPricing;
    }

    public Product[] getFreeItem() {
        return freeItem;
    }

    public float getFinalPrice() {
        return finalPrice;
    }

    public float getFreeItemTotalSavings() {
        return freeItemTotalSavings;
    }

    public float getListPrice() {
        return listPrice;
    }

    public String getUnitOfMeasure() {
        return unitOfMeasure;
    }
}
