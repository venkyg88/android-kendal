
package com.staples.mobile.common.access.easyopen.model.cart;

public class Pricing {

    private String buyMoreSaveMoreImage;
    private String displayRegularPricing;
    private String displayWasPricing;
    private float finalPrice;
    private float price;
    private float totalOrderItemPrice;
    private String unitOfMeasure;

    public String getBuyMoreSaveMoreImage() {
        return buyMoreSaveMoreImage;
    }

    public void setBuyMoreSaveMoreImage(String buyMoreSaveMoreImage) {
        this.buyMoreSaveMoreImage = buyMoreSaveMoreImage;
    }

    public String getDisplayRegularPricing() {
        return displayRegularPricing;
    }

    public void setDisplayRegularPricing(String displayRegularPricing) {
        this.displayRegularPricing = displayRegularPricing;
    }

    public String getDisplayWasPricing() {
        return displayWasPricing;
    }

    public void setDisplayWasPricing(String displayWasPricing) {
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

}
