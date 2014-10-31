/*
 * Copyright (c) 2014 Staples, Inc. All rights reserved.
 */

package com.staples.mobile.common.access.easyopen.model.cart;

/**
 * Created by Diana Sutlief.
 */
public class ShippingAddress {
    private String shippingAddressId;
    private String deliveryAddress1;
    private String deliveryAddress2;
    private String deliveryCity;
    private String deliveryFirstName;
    private String deliveryLastName;
    private String deliveryCompanyName;
    private String deliveryPhoneExtension;
    private String deliveryPhone;
    private String deliveryState;
    private String deliveryZipCode;
    private String deliveryLocation;
    private String emailAddress;
    private String reenterEmailAddress;

    // default constructor
    public ShippingAddress() { }

    // conversion constructor
    public ShippingAddress(Address address) {
        //this.shippingAddressId = ; // no address id avail
        this.deliveryAddress1 = address.getAddress1();
        this.deliveryAddress2 = address.getAddress2();
        this.deliveryCity = address.getCity();
        this.deliveryFirstName = address.getFirstName();
        this.deliveryLastName = address.getLastName();
        this.deliveryCompanyName = address.getOrganizationName();
        this.deliveryPhoneExtension = address.getPhoneExtension();
        this.deliveryPhone = address.getPhoneNumber();
        this.deliveryState = address.getState();
        this.deliveryZipCode = address.getZipCode();
        this.deliveryLocation = "shiptohome";
        this.emailAddress = address.getEmailAddress();
        this.reenterEmailAddress = address.getEmailAddress();
    }

    // conversion constructor
    public ShippingAddress(com.staples.mobile.common.access.easyopen.model.member.Address profileAddress) {
        this.shippingAddressId = profileAddress.getAddressId();
        this.deliveryAddress1 = profileAddress.getAddress1();
        this.deliveryAddress2 = profileAddress.getAddress2();
        this.deliveryCity = profileAddress.getCity();
        this.deliveryFirstName = profileAddress.getFirstname();
        this.deliveryLastName = profileAddress.getLastname();
        this.deliveryCompanyName = profileAddress.getOrganizationName();
        this.deliveryPhoneExtension = profileAddress.getPhoneExtension();
        this.deliveryPhone = profileAddress.getPhone1();
        this.deliveryState = profileAddress.getState();
        this.deliveryZipCode = profileAddress.getZipcode();
        this.deliveryLocation = "shiptohome";
        this.emailAddress = profileAddress.getEmailAddress();
        this.reenterEmailAddress = profileAddress.getEmailAddress();
    }

    public String getShippingAddressId() {
        return shippingAddressId;
    }

    public void setShippingAddressId(String shippingAddressId) {
        this.shippingAddressId = shippingAddressId;
    }

    public String getDeliveryAddress1() {
        return deliveryAddress1;
    }

    public void setDeliveryAddress1(String deliveryAddress1) {
        this.deliveryAddress1 = deliveryAddress1;
    }

    public String getDeliveryAddress2() {
        return deliveryAddress2;
    }

    public void setDeliveryAddress2(String deliveryAddress2) {
        this.deliveryAddress2 = deliveryAddress2;
    }

    public String getDeliveryCity() {
        return deliveryCity;
    }

    public void setDeliveryCity(String deliveryCity) {
        this.deliveryCity = deliveryCity;
    }

    public String getDeliveryFirstName() {
        return deliveryFirstName;
    }

    public void setDeliveryFirstName(String deliveryFirstName) {
        this.deliveryFirstName = deliveryFirstName;
    }

    public String getDeliveryLastName() {
        return deliveryLastName;
    }

    public void setDeliveryLastName(String deliveryLastName) {
        this.deliveryLastName = deliveryLastName;
    }

    public String getDeliveryCompanyName() {
        return deliveryCompanyName;
    }

    public void setDeliveryCompanyName(String deliveryCompanyName) {
        this.deliveryCompanyName = deliveryCompanyName;
    }

    public String getDeliveryPhoneExtension() {
        return deliveryPhoneExtension;
    }

    public void setDeliveryPhoneExtension(String deliveryPhoneExtension) {
        this.deliveryPhoneExtension = deliveryPhoneExtension;
    }

    public String getDeliveryPhone() {
        return deliveryPhone;
    }

    public void setDeliveryPhone(String deliveryPhone) {
        this.deliveryPhone = deliveryPhone;
    }

    public String getDeliveryState() {
        return deliveryState;
    }

    public void setDeliveryState(String deliveryState) {
        this.deliveryState = deliveryState;
    }

    public String getDeliveryZipCode() {
        return deliveryZipCode;
    }

    public void setDeliveryZipCode(String deliveryZipCode) {
        this.deliveryZipCode = deliveryZipCode;
    }

    public String getDeliveryLocation() {
        return deliveryLocation;
    }

    public void setDeliveryLocation(String deliveryLocation) {
        this.deliveryLocation = deliveryLocation;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getReenterEmailAddress() {
        return reenterEmailAddress;
    }

    public void setReenterEmailAddress(String reenterEmailAddress) {
        this.reenterEmailAddress = reenterEmailAddress;
    }
}
