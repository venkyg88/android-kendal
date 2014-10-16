
package com.staples.mobile.common.access.easyopen.model.cart;

import java.util.ArrayList;
import java.util.List;

public class ItemsAdded {

    private String message;
    private List<OrderItemId> orderItemIds = new ArrayList<OrderItemId>();

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<OrderItemId> getOrderItemIds() {
        return orderItemIds;
    }

    public void setOrderItemIds(List<OrderItemId> orderItemIds) {
        this.orderItemIds = orderItemIds;
    }

}
