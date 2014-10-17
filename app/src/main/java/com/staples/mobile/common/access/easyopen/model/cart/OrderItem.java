package com.staples.mobile.common.access.easyopen.model.cart;

import com.fasterxml.jackson.annotation.JsonInclude;

public class OrderItem {
//Since this class works for both add and update bodies, this needs to be specified so that
//when orderItemId is null for add queries, it is ignored by Jackson
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String orderItemId;
    private String partNumber_0;
    private int quantity_0;

    public String getOrderItemId() {
        return orderItemId;
    }

    public void setOrderItemId(String orderItemId) {
        this.orderItemId = orderItemId;
    }

    public String getPartNumber_0() {
        return partNumber_0;
    }

    public void setPartNumber_0(String partNumber_0) {
        this.partNumber_0 = partNumber_0;
    }

    public int getQuantity_0() {
        return quantity_0;
    }

    public void setQuantity_0(int quantity_0) {
        this.quantity_0 = quantity_0;
    }
}
