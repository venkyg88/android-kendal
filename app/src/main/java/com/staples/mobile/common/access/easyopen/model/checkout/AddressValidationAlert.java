package com.staples.mobile.common.access.easyopen.model.checkout;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.staples.mobile.common.access.easyopen.model.BaseResponse;

import java.lang.StringBuilder;
import java.lang.String;

@JsonPropertyOrder({"addressValidationAlert","city","state","zipCode"})
public class AddressValidationAlert extends BaseResponse {

    private String addressValidationAlert;
    private String city;
    private String state;
    private String zipCode;
    private String shippingAddressId; // needed for response when adding shipping address to cart
    private String billingAddressId; // needed for response when adding billing address to cart

    @JsonProperty("AddressValidationAlert")
    public String getAddressValidationAlert(){
        return this.addressValidationAlert;
    }

    public void setAddressValidationAlert(String addressValidationAlert){
        this.addressValidationAlert = addressValidationAlert;
    }

    @JsonProperty("City")
    public String getCity(){
        return this.city;
    }

    public void setCity(String city){
        this.city = city;
    }

    @JsonProperty("State")
    public String getState(){
        return this.state;
    }

    public void setState(String state){
        this.state = state;
    }

    @JsonProperty("ZipCode")
    public String getZipCode(){
        return this.zipCode;
    }

    public void setZipCode(String zipCode){
        this.zipCode = zipCode;
    }

    public String getShippingAddressId() {
        return shippingAddressId;
    }

    public void setShippingAddressId(String shippingAddressId) {
        this.shippingAddressId = shippingAddressId;
    }

    public String getBillingAddressId() {
        return billingAddressId;
    }

    public void setBillingAddressId(String billingAddressId) {
        this.billingAddressId = billingAddressId;
    }

    public String toString(){
        StringBuilder builder = new StringBuilder();
        builder.append("Precheckout[").append("addressValidationAlert : ").append(addressValidationAlert).append(",\n")
               .append("city : ").append(city).append(",\n")
               .append("state : ").append(state).append(",\n")
               .append("zipCode : ").append(zipCode).append(",\n")
               .append("]");
        return builder.toString();
    }

}
