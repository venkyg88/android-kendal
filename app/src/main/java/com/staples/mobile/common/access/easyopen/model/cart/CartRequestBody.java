package com.staples.mobile.common.access.easyopen.model.cart;

import java.util.List;

//wrapper class for cart add/update POST data
public class CartRequestBody {
    private List<OrderItem> orderItem;

    public List<OrderItem> getOrderItem() {
        return orderItem;
    }

    public void setOrderItem(List<OrderItem> orderItem) {
        this.orderItem = orderItem;
    }
}