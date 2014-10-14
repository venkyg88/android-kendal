
package com.staples.mobile.common.access.easyopen.model.cart;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

public class ViewCart {
    @JsonProperty("Cart")
    private List<Cart> cart = new ArrayList<Cart>();
    private String recordSetComplete;
    private String recordSetCount;
    private String recordSetStartNumber;
    private String recordSetTotal;

    public List<Cart> getCart() {
        return cart;
    }

    public void setCart(List<Cart> cart) {
        this.cart = cart;
    }

    public String getRecordSetComplete() {
        return recordSetComplete;
    }

    public void setRecordSetComplete(String recordSetComplete) {
        this.recordSetComplete = recordSetComplete;
    }

    public String getRecordSetCount() {
        return recordSetCount;
    }

    public void setRecordSetCount(String recordSetCount) {
        this.recordSetCount = recordSetCount;
    }

    public String getRecordSetStartNumber() {
        return recordSetStartNumber;
    }

    public void setRecordSetStartNumber(String recordSetStartNumber) {
        this.recordSetStartNumber = recordSetStartNumber;
    }

    public String getRecordSetTotal() {
        return recordSetTotal;
    }

    public void setRecordSetTotal(String recordSetTotal) {
        this.recordSetTotal = recordSetTotal;
    }

}
