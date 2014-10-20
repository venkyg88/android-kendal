package com.staples.mobile.common.access.easyopen.model.cart;

import com.fasterxml.jackson.annotation.JsonInclude;

public class OrderItem {
//Since this class works for both add and update bodies, this needs to be specified so that
//when orderItemId is null for add queries, it is ignored by Jackson
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String orderItemId;
    private String partNumber;
    private int quantity;

    public OrderItem(String orderItemId, String partNumber, int quantity) {
        this.orderItemId = orderItemId;
        this.partNumber = partNumber;
        this.quantity = quantity;
    }


    public String getOrderItemId() {
        return orderItemId;
    }

    public void setOrderItemId(String orderItemId) {
        this.orderItemId = orderItemId;
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
}
