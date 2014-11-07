package com.staples.mobile.common.access.easyopen.model.cart;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.staples.mobile.common.access.easyopen.model.BaseResponse;

import java.util.List;

/**
 * Created by Avinash Dodda.
 */
public class AddressDetail extends BaseResponse {

    @JsonProperty("Cart")
    private List<Address> address;

    public List<Address> getAddress() {
        return address;
    }

    public void setAddress(List<Address> address) {
        this.address = address;
    }
}
