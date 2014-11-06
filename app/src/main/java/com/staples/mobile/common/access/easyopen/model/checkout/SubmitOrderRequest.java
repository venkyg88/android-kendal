package com.staples.mobile.common.access.easyopen.model.checkout;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.lang.StringBuilder;
import java.lang.String;

@JsonPropertyOrder({"purchaseOrderNumber", "cardVerificationCode"})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SubmitOrderRequest {

    private String purchaseOrderNumber;
    private String cardVerificationCode;

    @JsonProperty("purchaseOrderNumber")
    public String getPurchaseOrderNumber() {
        return this.purchaseOrderNumber;
    }

    public void setPurchaseOrderNumber(String purchaseOrderNumber) {
        this.purchaseOrderNumber = purchaseOrderNumber;
    }

    @JsonProperty("cardVerificationCode")
    public String getCardVerificationCode() {
        return this.cardVerificationCode;
    }

    public void setCardVerificationCode(String cardVerificationCode) {
        this.cardVerificationCode = cardVerificationCode;
    }


    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("SubmitOrderRequest[").append("purchaseOrderNumber : ").append(purchaseOrderNumber).append(",\n")
                .append("cardVerificationCode : ").append(cardVerificationCode).append(",\n")
                .append("]");
        return builder.toString();
    }

}
