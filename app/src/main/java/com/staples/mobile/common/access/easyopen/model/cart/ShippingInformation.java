package com.staples.mobile.common.access.easyopen.model.cart;

public class ShippingInformation {
    private DeliveryAddress deliveryAddressSelected;
    private DeliveryModeAvailable deliveryModeAvailable;
    private String deliveryModeSelected;
    private boolean deliveryPromotion;
    private Object estimatedDeliveryDate;
    private int estimatedDeliveryInBusinessDays;
    private boolean quickShip;

    public DeliveryAddress getDeliveryAddressSelected() {
        return deliveryAddressSelected;
    }

    public DeliveryModeAvailable getDeliveryModeAvailable() {
        return deliveryModeAvailable;
    }

    public String getDeliveryModeSelected() {
        return deliveryModeSelected;
    }

    public boolean isDeliveryPromotion() {
        return deliveryPromotion;
    }

    public Object getEstimatedDeliveryDate() {
        return estimatedDeliveryDate;
    }

    public int getEstimatedDeliveryInBusinessDays() {
        return estimatedDeliveryInBusinessDays;
    }

    public boolean isQuickShip() {
        return quickShip;
    }
}
