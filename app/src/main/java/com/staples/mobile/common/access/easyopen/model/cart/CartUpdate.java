
package com.staples.mobile.common.access.easyopen.model.cart;

import java.util.ArrayList;
import java.util.List;

//wrapper class for add and update api responses

public class CartUpdate {

    private List<ItemsAdded> itemsAdded = new ArrayList<ItemsAdded>();

    public List<ItemsAdded> getItemsAdded() {
        return itemsAdded;
    }

    public void setItemsAdded(List<ItemsAdded> itemsAdded) {
        this.itemsAdded = itemsAdded;
    }

}
