package com.staples.mobile.common.access.easyopen.model.shipping;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.lang.StringBuilder;
import java.lang.String;

@JsonPropertyOrder({"orderIdentifier","shippingCharge"})
public class Cart {

    private String orderIdentifier;
    private String shippingCharge;

    @JsonProperty("orderIdentifier")
    public String getOrderIdentifier(){
        return this.orderIdentifier;
    }

    public void setOrderIdentifier(String orderIdentifier){
        this.orderIdentifier = orderIdentifier;
    }

    @JsonProperty("shippingCharge")
    public String getShippingCharge(){
        return this.shippingCharge;
    }

    public void setShippingCharge(String shippingCharge){
        this.shippingCharge = shippingCharge;
    }




    public String toString(){
        StringBuilder builder = new StringBuilder();
        builder.append("Cart[").append("orderIdentifier : ").append(orderIdentifier).append(",\n")
               .append("shippingCharge : ").append(shippingCharge).append(",\n")
               .append("]");
        return builder.toString();
    }

}
