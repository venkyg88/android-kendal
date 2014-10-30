package com.staples.mobile.common.access.easyopen.model.member;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Created by Avinash Dodda.
 */
public class AddressDetail {

    @JsonProperty("Cart")
    private List<Address> address;

    public List<Address> getAddress() {
        return address;
    }

    public void setAddress(List<Address> address) {
        this.address = address;
    }
}
