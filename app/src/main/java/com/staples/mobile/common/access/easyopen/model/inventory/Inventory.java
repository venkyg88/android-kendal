
package com.staples.mobile.common.access.easyopen.model.inventory;

public class Inventory {

    private boolean available;
    private boolean bopisEligible;
    private String partNumber;
    private float quantity;
    private String stockLevelMessage;

    public boolean getAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public boolean getBopisEligible() {
        return bopisEligible;
    }

    public void setBopisEligible(boolean bopisEligible) {
        this.bopisEligible = bopisEligible;
    }

    public String getPartNumber() {
        return partNumber;
    }

    public void setPartNumber(String partNumber) {
        this.partNumber = partNumber;
    }

    public float getQuantity() {
        return quantity;
    }

    public void setQuantity(float quantity) {
        this.quantity = quantity;
    }

    public String getStockLevelMessage() {
        return stockLevelMessage;
    }

    public void setStockLevelMessage(String stockLevelMessage) {
        this.stockLevelMessage = stockLevelMessage;
    }

}
