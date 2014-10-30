
package com.staples.mobile.common.access.easyopen.model.cart;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class Pricing {

    @JsonProperty("BuyMoreSaveMoreDetail")
    private List<BuyMoreSaveMoreDetail> buyMoreSaveMoreDetail;
    private String buyMoreSaveMoreImage;
    private boolean displayRegularPricing;
    private boolean displayWasPricing;
    private float finalPrice;
    private float listPrice;
    private float price;
    private float totalOrderItemPrice;
    private String unitOfMeasure;
    private String savings;

    public String getBuyMoreSaveMoreImage() {
        return buyMoreSaveMoreImage;
    }

    public void setBuyMoreSaveMoreImage(String buyMoreSaveMoreImage) {
        this.buyMoreSaveMoreImage = buyMoreSaveMoreImage;
    }

    public boolean getDisplayRegularPricing() {
        return displayRegularPricing;
    }

    public void setDisplayRegularPricing(boolean displayRegularPricing) {
        this.displayRegularPricing = displayRegularPricing;
    }

    public boolean getDisplayWasPricing() {
        return displayWasPricing;
    }

    public void setDisplayWasPricing(boolean displayWasPricing) {
        this.displayWasPricing = displayWasPricing;
    }

    public float getFinalPrice() {
        return finalPrice;
    }

    public void setFinalPrice(float finalPrice) {
        this.finalPrice = finalPrice;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public float getTotalOrderItemPrice() {
        return totalOrderItemPrice;
    }

    public void setTotalOrderItemPrice(float totalOrderItemPrice) {
        this.totalOrderItemPrice = totalOrderItemPrice;
    }

    public String getUnitOfMeasure() {
        return unitOfMeasure;
    }

    public void setUnitOfMeasure(String unitOfMeasure) {
        this.unitOfMeasure = unitOfMeasure;
    }

    public List<BuyMoreSaveMoreDetail> getBuyMoreSaveMoreDetail() {
        return buyMoreSaveMoreDetail;
    }

    public void setBuyMoreSaveMoreDetail(List<BuyMoreSaveMoreDetail> buyMoreSaveMoreDetail) {
        this.buyMoreSaveMoreDetail = buyMoreSaveMoreDetail;
    }

    public float getListPrice() {
        return listPrice;
    }

    public void setListPrice(float listPrice) {
        this.listPrice = listPrice;
    }

    public String getSavings() {
        return savings;
    }

    public void setSavings(String savings) {
        this.savings = savings;
    }
}
