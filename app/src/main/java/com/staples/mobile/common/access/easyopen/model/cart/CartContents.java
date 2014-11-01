
package com.staples.mobile.common.access.easyopen.model.cart;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.staples.mobile.common.access.easyopen.model.ApiError;
import com.staples.mobile.common.access.easyopen.model.SupportsApiErrors;

import java.util.ArrayList;
import java.util.List;

//wrapper class for view cart api responses

public class CartContents implements SupportsApiErrors {
    @JsonProperty("Cart")
    private List<Cart> cart = new ArrayList<Cart>();
    private boolean recordSetComplete;
    private int recordSetCount;
    private int recordSetStartNumber;
    private int recordSetTotal;

    // include this so that ((CartContents)retrofitError.getBody()).getErrors()
    // can be examined in 400 Bad Request failure response (e.g. in debugger)
    private List<ApiError> errors;
    public List<ApiError> getErrors() { return errors; }
    public void setErrors(List<ApiError> errors) { this.errors = errors; }

    public List<Cart> getCart() {
        return cart;
    }

    public void setCart(List<Cart> cart) {
        this.cart = cart;
    }

    public boolean getRecordSetComplete() {
        return recordSetComplete;
    }

    public void setRecordSetComplete(boolean recordSetComplete) {
        this.recordSetComplete = recordSetComplete;
    }

    public int getRecordSetCount() {
        return recordSetCount;
    }

    public void setRecordSetCount(int recordSetCount) {
        this.recordSetCount = recordSetCount;
    }

    public int getRecordSetStartNumber() {
        return recordSetStartNumber;
    }

    public void setRecordSetStartNumber(int recordSetStartNumber) {
        this.recordSetStartNumber = recordSetStartNumber;
    }

    public int getRecordSetTotal() {
        return recordSetTotal;
    }

    public void setRecordSetTotal(int recordSetTotal) {
        this.recordSetTotal = recordSetTotal;
    }

}
