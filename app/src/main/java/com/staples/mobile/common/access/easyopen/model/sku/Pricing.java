package com.staples.mobile.common.access.easyopen.model.sku;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class Pricing {
    @JsonProperty("BuyMoreSaveMoreDetail")
    List<BuyMoreSaveMoreDetail> buyMoreSaveMoreDetail;
    String buyMoreSaveMoreImage;
    List<Description> description;
    List<Discount> discount;
    boolean displayRegularPricing;
    boolean displayWasPricing;
    float finalPriceDeduction;
    List<Product> freeItem;
    float finalPrice;
    float freeItemTotalSavings;
    List<Image> image;
    float listPrice;
    float price;
    float savings;
    String unitOfMeasure;

    public List<BuyMoreSaveMoreDetail> getBuyMoreSaveMoreDetail() {
        return buyMoreSaveMoreDetail;
    }

    public String getBuyMoreSaveMoreImage() {
        return buyMoreSaveMoreImage;
    }

    public List<Description> getDescription() {
        return description;
    }

    public List<Discount> getDiscount() {
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

    public List<Product> getFreeItem() {
        return freeItem;
    }

    public float getFinalPrice() {
        return finalPrice;
    }

    public float getFreeItemTotalSavings() {
        return freeItemTotalSavings;
    }

    public List<Image> getImage() {
        return image;
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
