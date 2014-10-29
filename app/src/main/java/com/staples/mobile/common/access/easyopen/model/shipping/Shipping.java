package com.staples.mobile.common.access.easyopen.model.shipping;

import java.util.ArrayList;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import com.staples.mobile.common.access.easyopen.model.shipping.Cart;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.lang.StringBuilder;

@JsonPropertyOrder({"cart"})
public class Shipping {

    private List<Cart> cart = new ArrayList<Cart> ();

    @JsonProperty("Cart")
    public List<Cart> getCart(){
        return this.cart;
    }

    public void setCart(List<Cart> cart){
        this.cart = cart;
    }




    public String toString(){
        StringBuilder builder = new StringBuilder();
        builder.append("Shipping[").append("cart : ").append(cart).append(",\n")
               .append("]");
        return builder.toString();
    }

}
