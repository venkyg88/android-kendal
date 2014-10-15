
package com.staples.mobile.common.access.easyopen.model.cart;

public class DeliveryModeAvailable {

    private Object pickUp;
    private String shipToAddress;
    private String shipToStore;

    public Object getPickUp() {
        return pickUp;
    }

    public void setPickUp(Object pickUp) {
        this.pickUp = pickUp;
    }

    public String getShipToAddress() {
        return shipToAddress;
    }

    public void setShipToAddress(String shipToAddress) {
        this.shipToAddress = shipToAddress;
    }

    public String getShipToStore() {
        return shipToStore;
    }

    public void setShipToStore(String shipToStore) {
        this.shipToStore = shipToStore;
    }
}
