
package com.staples.mobile.common.access.easyopen.model.cart;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.ArrayList;
import java.util.List;

public class CartUpdate {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String message;

    private List<ItemsAdded> itemsAdded = new ArrayList<ItemsAdded>();

    public List<ItemsAdded> getItemsAdded() {
        return itemsAdded;
    }

    public void setItemsAdded(List<ItemsAdded> itemsAdded) {
        this.itemsAdded = itemsAdded;
    }

    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }

}
