
package com.staples.mobile.common.access.easyopen.model.cart;

public class ShippingInformation {

    private String deliveryAddressSelected;
    private DeliveryModeAvailable deliveryModeAvailable;
    private String deliveryModeSelected;
    private boolean deliveryPromotion;
    private Object estimatedDeliveryDate;
    private int estimatedDeliveryInBusinessDays;
    private boolean quickShip;

    public String getDeliveryAddressSelected() {
        return deliveryAddressSelected;
    }

    public void setDeliveryAddressSelected(String deliveryAddressSelected) {
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

    public boolean getDeliveryPromotion() {
        return deliveryPromotion;
    }

    public void setDeliveryPromotion(boolean deliveryPromotion) {
        this.deliveryPromotion = deliveryPromotion;
    }

    public Object getEstimatedDeliveryDate() {
        return estimatedDeliveryDate;
    }

    public void setEstimatedDeliveryDate(Object estimatedDeliveryDate) {
        this.estimatedDeliveryDate = estimatedDeliveryDate;
    }

    public int getEstimatedDeliveryInBusinessDays() {
        return estimatedDeliveryInBusinessDays;
    }

    public void setEstimatedDeliveryInBusinessDays(int estimatedDeliveryInBusinessDays) {
        this.estimatedDeliveryInBusinessDays = estimatedDeliveryInBusinessDays;
    }

    public boolean getQuickShip() {
        return quickShip;
    }

    public void setQuickShip(boolean quickShip) {
        this.quickShip = quickShip;
    }

}
