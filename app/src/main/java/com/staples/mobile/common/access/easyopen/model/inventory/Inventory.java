
package com.staples.mobile.common.access.easyopen.model.inventory;

public class Inventory {

    private boolean available;
    private boolean bopisEligible;
    private String partNumber;
    //TODO make sure quantity doesn't give back decimal values
    private int quantity;
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

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getStockLevelMessage() {
        return stockLevelMessage;
    }

    public void setStockLevelMessage(String stockLevelMessage) {
        this.stockLevelMessage = stockLevelMessage;
    }

}
