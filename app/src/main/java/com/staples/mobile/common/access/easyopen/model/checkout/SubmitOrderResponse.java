package com.staples.mobile.common.access.easyopen.model.checkout;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.staples.mobile.common.access.easyopen.model.ApiError;
import com.staples.mobile.common.access.easyopen.model.SupportsApiErrors;

import java.lang.StringBuilder;
import java.lang.String;
import java.util.List;

@JsonPropertyOrder({"orderId", "staplesOrderNumber"})
public class SubmitOrderResponse implements SupportsApiErrors {

    private String orderId;
    private String staplesOrderNumber;

    // include this so that ((SubmitOrderResponse)retrofitError.getBody()).getErrors()
    // can be examined in 400 Bad Request failure response (e.g. in debugger)
    private List<ApiError> errors;
    public List<ApiError> getErrors() { return errors; }
    public void setErrors(List<ApiError> errors) { this.errors = errors; }

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
