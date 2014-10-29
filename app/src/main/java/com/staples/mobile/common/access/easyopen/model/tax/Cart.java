package com.staples.mobile.common.access.easyopen.model.tax;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.lang.StringBuilder;
import java.lang.String;

@JsonPropertyOrder({"orderIdentifier", "totalTax"})
public class Cart {

    private String orderIdentifier;
    private String totalTax;

    @JsonProperty("orderIdentifier")
    public String getOrderIdentifier() {
        return this.orderIdentifier;
    }

    public void setOrderIdentifier(String orderIdentifier) {
        this.orderIdentifier = orderIdentifier;
    }

    @JsonProperty("totalTax")
    public String getTotalTax() {
        return this.totalTax;
    }

    public void setTotalTax(String totalTax) {
        this.totalTax = totalTax;
    }


    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Cart[").append("orderIdentifier : ").append(orderIdentifier).append(",\n")
                .append("totalTax : ").append(totalTax).append(",\n")
                .append("]");
        return builder.toString();
    }

}
