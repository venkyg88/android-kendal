/*
 * Copyright (c) 2014 Staples, Inc. All rights reserved.
 */

package com.staples.mobile.common.access.easyopen.model.cart;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Created by Diana Sutlief.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BillingAddress {
    private String billingAddress1;
    private String billingAddress2;
    private String billingCity;
    private String billingFirstName;
    private String billingLastName;
    private String billingCompanyName;
    private String billingPhoneExtension;
    private String billingPhone;
    private String billingState;
    private String billingZipCode;

    // default constructor
    public BillingAddress() { }

    // conversion constructor
    public BillingAddress(Address address) {
        this.billingAddress1 = address.getAddress1();
        this.billingAddress2 = address.getAddress2();
        this.billingCity = address.getCity();
        this.billingFirstName = address.getFirstName();
        this.billingLastName = address.getLastName();
        this.billingCompanyName = address.getOrganizationName();
        this.billingPhoneExtension = address.getPhoneExtension();
        this.billingPhone = address.getPhoneNumber();
        this.billingState = address.getState();
        this.billingZipCode = address.getZipCode();
    }

    // conversion constructor
    public BillingAddress(com.staples.mobile.common.access.easyopen.model.member.Address profileAddress) {
        this.billingAddress1 = profileAddress.getAddress1();
        this.billingAddress2 = profileAddress.getAddress2();
        this.billingCity = profileAddress.getCity();
        this.billingFirstName = profileAddress.getFirstname();
        this.billingLastName = profileAddress.getLastname();
        this.billingCompanyName = profileAddress.getOrganizationName();
        this.billingPhoneExtension = profileAddress.getPhoneExtension();
        this.billingPhone = profileAddress.getPhone1();
        this.billingState = profileAddress.getState();
        this.billingZipCode = profileAddress.getZipcode();
    }


    // conversion constructor
    public BillingAddress(ShippingAddress shippingAddress) {
        this.billingAddress1 = shippingAddress.getDeliveryAddress1();
        this.billingAddress2 = shippingAddress.getDeliveryAddress2();
        this.billingCity = shippingAddress.getDeliveryCity();
        this.billingFirstName = shippingAddress.getDeliveryFirstName();
        this.billingLastName = shippingAddress.getDeliveryLastName();
        this.billingCompanyName = shippingAddress.getDeliveryCompanyName();
        this.billingPhoneExtension = shippingAddress.getDeliveryPhoneExtension();
        this.billingPhone = shippingAddress.getDeliveryPhone();
        this.billingState = shippingAddress.getDeliveryState();
        this.billingZipCode = shippingAddress.getDeliveryZipCode();
    }

    public String getBillingAddress1() {
        return billingAddress1;
    }

    public void setBillingAddress1(String billingAddress1) {
        this.billingAddress1 = billingAddress1;
    }

    public String getBillingAddress2() {
        return billingAddress2;
    }

    public void setBillingAddress2(String billingAddress2) {
        this.billingAddress2 = billingAddress2;
    }

    public String getBillingCity() {
        return billingCity;
    }

    public void setBillingCity(String billingCity) {
        this.billingCity = billingCity;
    }

    public String getBillingFirstName() {
        return billingFirstName;
    }

    public void setBillingFirstName(String billingFirstName) {
        this.billingFirstName = billingFirstName;
    }

    public String getBillingLastName() {
        return billingLastName;
    }

    public void setBillingLastName(String billingLastName) {
        this.billingLastName = billingLastName;
    }

    public String getBillingCompanyName() {
        return billingCompanyName;
    }

    public void setBillingCompanyName(String billingCompanyName) {
        this.billingCompanyName = billingCompanyName;
    }

    public String getBillingPhoneExtension() {
        return billingPhoneExtension;
    }

    public void setBillingPhoneExtension(String billingPhoneExtension) {
        this.billingPhoneExtension = billingPhoneExtension;
    }

    public String getBillingPhone() {
        return billingPhone;
    }

    public void setBillingPhone(String billingPhone) {
        this.billingPhone = billingPhone;
    }

    public String getBillingState() {
        return billingState;
    }

    public void setBillingState(String billingState) {
        this.billingState = billingState;
    }

    public String getBillingZipCode() {
        return billingZipCode;
    }

    public void setBillingZipCode(String billingZipCode) {
        this.billingZipCode = billingZipCode;
    }
}
