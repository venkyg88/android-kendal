
package com.staples.mobile.common.access.easyopen.model.cart;

public class ShippingInformation {

    private Object deliveryAddressSelected;
    private DeliveryModeAvailable deliveryModeAvailable;
    private String deliveryModeSelected;
    private String deliveryPromotion;
    private Object estimatedDeliveryDate;
    private String estimatedDeliveryInBusinessDays;
    private String quickShip;

    public Object getDeliveryAddressSelected() {
        return deliveryAddressSelected;
    }

    public void setDeliveryAddressSelected(Object deliveryAddressSelected) {
        this.deliveryAddressSelected = deliveryAddressSelected;
    }

    public DeliveryModeAvailable getDeliveryModeAvailable() {
        return deliveryModeAvailable;
    }

    public void setDeliveryModeAvailable(DeliveryModeAvailable deliveryModeAvailable) {
        this.deliveryModeAvailable = deliveryModeAvailable;
    }

    public String getDeliveryModeSelected() {
        return deliveryModeSelected;
    }

    public void setDeliveryModeSelected(String deliveryModeSelected) {
        this.deliveryModeSelected = deliveryModeSelected;
    }

    public String getDeliveryPromotion() {
        return deliveryPromotion;
    }

    public void setDeliveryPromotion(String deliveryPromotion) {
        this.deliveryPromotion = deliveryPromotion;
    }

    public Object getEstimatedDeliveryDate() {
        return estimatedDeliveryDate;
    }

    public void setEstimatedDeliveryDate(Object estimatedDeliveryDate) {
        this.estimatedDeliveryDate = estimatedDeliveryDate;
    }

    public String getEstimatedDeliveryInBusinessDays() {
        return estimatedDeliveryInBusinessDays;
    }

    public void setEstimatedDeliveryInBusinessDays(String estimatedDeliveryInBusinessDays) {
        this.estimatedDeliveryInBusinessDays = estimatedDeliveryInBusinessDays;
    }

    public String getQuickShip() {
        return quickShip;
    }

    public void setQuickShip(String quickShip) {
        this.quickShip = quickShip;
    }

}
