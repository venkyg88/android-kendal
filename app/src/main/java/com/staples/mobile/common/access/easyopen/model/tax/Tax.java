package com.staples.mobile.common.access.easyopen.model.tax;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.ArrayList;
import java.util.List;

@JsonPropertyOrder({"cart"})
public class Tax {

    private List<Cart> cart = new ArrayList<Cart>();

    @JsonProperty("Cart")
    public List<Cart> getCart() {
        return this.cart;
    }

    public void setCart(List<Cart> cart) {
        this.cart = cart;
    }


    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Tax[").append("cart : ").append(cart).append(",\n")
                .append("]");
        return builder.toString();
    }

}
