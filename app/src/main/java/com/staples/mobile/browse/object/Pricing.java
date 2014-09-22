package com.staples.mobile.browse.object;

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
    private float finalPrice;
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

    public float getFinalPrice() {
        return finalPrice;
    }

    public String getUnitOfMeasure() {
        return unitOfMeasure;
    }
}
