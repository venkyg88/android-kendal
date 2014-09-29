package com.staples.mobile.common.access.easyopen.model.browse;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by pyhre001 on 9/22/14.
 */
public class Pricing {
    @JsonProperty("BuyMoreSaveMoreDetail")
    private BuyMoreSaveMoreDetail[] buyMoreSaveMoreDetail;
    private String buyMoreSaveMoreImage;
    private Discount[] discount;
    private boolean displayRegularPricing;
    private boolean displayWasPricing;
    private float finalPriceDeduction;
    private Product[] freeItem;
    private float finalPrice;
    private float freeItemTotalSavings;
    private float listPrice;
    private float price;
    private String unitOfMeasure;

    public BuyMoreSaveMoreDetail[] getBuyMoreSaveMoreDetail() {
        return buyMoreSaveMoreDetail;
    }

    public String getBuyMoreSaveMoreImage() {
        return buyMoreSaveMoreImage;
    }

    public Discount[] getDiscount() {
        return discount;
    }

    public boolean isDisplayRegularPricing() {
        return displayRegularPricing;
    }

    public boolean isDisplayWasPricing() {
        return displayWasPricing;
    }

    public float getFinalPriceDeduction() {
        return finalPriceDeduction;
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

    public float getPrice() {
        return price;
    }

    public String getUnitOfMeasure() {
        return unitOfMeasure;
    }
}
