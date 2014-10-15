package com.staples.mobile.common.access.easyopen.model.cart;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class CartUpdateBody {
    @JsonProperty("orderItem")
    private List<UpdateOrderItem> updateOrderItem;

    public List<UpdateOrderItem> getUpdateOrderItem() {
        return updateOrderItem;
    }

    public void setUpdateOrderItem(List<UpdateOrderItem> updateOrderItem) {
        this.updateOrderItem = updateOrderItem;
    }
}
