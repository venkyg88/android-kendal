package com.staples.mobile.common.access.easyopen.model.browse;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Pricing {
    @JsonProperty("BuyMoreSaveMoreDetail")
    private BuyMoreSaveMoreDetail[] buyMoreSaveMoreDetail;
    private String buyMoreSaveMoreImage;
    private Description[] description;
    private Discount[] discount;
    private boolean displayRegularPricing;
    private boolean displayWasPricing;
    private float finalPriceDeduction;
    private Product[] freeItem;
    private float finalPrice;
    private float freeItemTotalSavings;
    private float listPrice;
    private float price;
    private float savings;
    private String unitOfMeasure;

    public BuyMoreSaveMoreDetail[] getBuyMoreSaveMoreDetail() {
        return buyMoreSaveMoreDetail;
    }

    public String getBuyMoreSaveMoreImage() {
        return buyMoreSaveMoreImage;
    }

    public Description[] getDescription() {
        return description;
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

    public float getSavings() {
        return savings;
    }

    public String getUnitOfMeasure() {
        return unitOfMeasure;
    }
}
