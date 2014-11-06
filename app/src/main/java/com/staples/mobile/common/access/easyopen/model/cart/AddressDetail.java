package com.staples.mobile.common.access.easyopen.model.cart;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.staples.mobile.common.access.easyopen.model.ApiError;
import com.staples.mobile.common.access.easyopen.model.SupportsApiErrors;

import java.util.List;

/**
 * Created by Avinash Dodda.
 */
public class AddressDetail implements SupportsApiErrors {

    @JsonProperty("Cart")
    private List<Address> address;
    private List<ApiError> errors; // this allows one to examine ((AddressDetail)retrofitError.getBody()).getErrors() when 400 Bad Request

    public List<Address> getAddress() {
        return address;
    }

    public void setAddress(List<Address> address) {
        this.address = address;
    }

    public List<ApiError> getErrors() {
        return errors;
    }

    public void setErrors(List<ApiError> errors) {
        this.errors = errors;
    }
}
