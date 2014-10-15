package com.staples.mobile.common.access.easyopen.model.cart;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class CartAddBody {
    @JsonProperty("orderItem")
    private List<AddOrderItem> addOrderItem;

    public List<AddOrderItem> getAddOrderItem() {
        return addOrderItem;
    }

    public void setAddOrderItem(List<AddOrderItem> addOrderItem) {
        this.addOrderItem = addOrderItem;
    }
}
