package com.staples.mobile.common.access.easyopen.model.checkout;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.staples.mobile.common.access.easyopen.model.ApiError;
import com.staples.mobile.common.access.easyopen.model.BaseResponse;

import java.lang.StringBuilder;
import java.lang.String;

@JsonPropertyOrder({"orderId", "staplesOrderNumber"})
public class SubmitOrderResponse extends BaseResponse {

    private String orderId;
    private String staplesOrderNumber;

    @JsonProperty("orderId")
    public String getOrderId() {
        return this.orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    @JsonProperty("staplesOrderNumber")
    public String getStaplesOrderNumber() {
        return this.staplesOrderNumber;
    }

    public void setStaplesOrderNumber(String staplesOrderNumber) {
        this.staplesOrderNumber = staplesOrderNumber;
    }


    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("SubmitOrderResponse[").append("orderId : ").append(orderId).append(",\n")
                .append("staplesOrderNumber : ").append(staplesOrderNumber).append(",\n")
                .append("]");
        return builder.toString();
    }

}
