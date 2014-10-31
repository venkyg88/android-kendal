/*
 * Copyright (c) 2014 Staples, Inc. All rights reserved.
 */

package com.staples.mobile.common.access.easyopen.model.cart;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


/**
 * Created by sutdi001 on 10/30/14.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderStatus {
    String billtoAddress1;
    String billtoAddress2;
    String billtoCity;
    String billtoCompanyName;
    String billtoFirstName;
    String billtoLastName;
    String billtoState;
    String billtoZip;
    String ccExpirationDt;
    String ccNumber;
    String ccType;
    String completionCode;
    String couponTotal;
    String grandTotal;
    String orderDate;
    String orderNumber;
    String paymentMethod;
    String salesTaxTotal;

    String shipmentSkuSubtotal;
    String shippingAndHandlingTotal;
    String shiptoAddress1;
    String shiptoAddress2;
    String shiptoCity;
    String shiptoCompanyName;
    String shiptoFirstName;
    String shiptoLastName;
    String shiptoState;
    String shiptoZip;
    String total;

    public String getBilltoAddress1() {
        return billtoAddress1;
    }

    public void setBilltoAddress1(String billtoAddress1) {
        this.billtoAddress1 = billtoAddress1;
    }

    public String getBilltoAddress2() {
        return billtoAddress2;
    }

    public void setBilltoAddress2(String billtoAddress2) {
        this.billtoAddress2 = billtoAddress2;
    }

    public String getBilltoCity() {
        return billtoCity;
    }

    public void setBilltoCity(String billtoCity) {
        this.billtoCity = billtoCity;
    }

    public String getBilltoCompanyName() {
        return billtoCompanyName;
    }

    public void setBilltoCompanyName(String billtoCompanyName) {
        this.billtoCompanyName = billtoCompanyName;
    }

    public String getBilltoFirstName() {
        return billtoFirstName;
    }

    public void setBilltoFirstName(String billtoFirstName) {
        this.billtoFirstName = billtoFirstName;
    }

    public String getBilltoLastName() {
        return billtoLastName;
    }

    public void setBilltoLastName(String billtoLastName) {
        this.billtoLastName = billtoLastName;
    }

    public String getBilltoState() {
        return billtoState;
    }

    public void setBilltoState(String billtoState) {
        this.billtoState = billtoState;
    }

    public String getBilltoZip() {
        return billtoZip;
    }

    public void setBilltoZip(String billtoZip) {
        this.billtoZip = billtoZip;
    }

    public String getCcExpirationDt() {
        return ccExpirationDt;
    }

    public void setCcExpirationDt(String ccExpirationDt) {
        this.ccExpirationDt = ccExpirationDt;
    }

    public String getCcNumber() {
        return ccNumber;
    }

    public void setCcNumber(String ccNumber) {
        this.ccNumber = ccNumber;
    }

    public String getCcType() {
        return ccType;
    }

    public void setCcType(String ccType) {
        this.ccType = ccType;
    }

    public String getCompletionCode() {
        return completionCode;
    }

    public void setCompletionCode(String completionCode) {
        this.completionCode = completionCode;
    }

    public String getCouponTotal() {
        return couponTotal;
    }

    public void setCouponTotal(String couponTotal) {
        this.couponTotal = couponTotal;
    }

    public String getGrandTotal() {
        return grandTotal;
    }

    public void setGrandTotal(String grandTotal) {
        this.grandTotal = grandTotal;
    }

    public String getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(String orderDate) {
        this.orderDate = orderDate;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getSalesTaxTotal() {
        return salesTaxTotal;
    }

    public void setSalesTaxTotal(String salesTaxTotal) {
        this.salesTaxTotal = salesTaxTotal;
    }

    public String getShipmentSkuSubtotal() {
        return shipmentSkuSubtotal;
    }

    public void setShipmentSkuSubtotal(String shipmentSkuSubtotal) {
        this.shipmentSkuSubtotal = shipmentSkuSubtotal;
    }

    public String getShippingAndHandlingTotal() {
        return shippingAndHandlingTotal;
    }

    public void setShippingAndHandlingTotal(String shippingAndHandlingTotal) {
        this.shippingAndHandlingTotal = shippingAndHandlingTotal;
    }

    public String getShiptoAddress1() {
        return shiptoAddress1;
    }

    public void setShiptoAddress1(String shiptoAddress1) {
        this.shiptoAddress1 = shiptoAddress1;
    }

    public String getShiptoAddress2() {
        return shiptoAddress2;
    }

    public void setShiptoAddress2(String shiptoAddress2) {
        this.shiptoAddress2 = shiptoAddress2;
    }

    public String getShiptoCity() {
        return shiptoCity;
    }

    public void setShiptoCity(String shiptoCity) {
        this.shiptoCity = shiptoCity;
    }

    public String getShiptoCompanyName() {
        return shiptoCompanyName;
    }

    public void setShiptoCompanyName(String shiptoCompanyName) {
        this.shiptoCompanyName = shiptoCompanyName;
    }

    public String getShiptoFirstName() {
        return shiptoFirstName;
    }

    public void setShiptoFirstName(String shiptoFirstName) {
        this.shiptoFirstName = shiptoFirstName;
    }

    public String getShiptoLastName() {
        return shiptoLastName;
    }

    public void setShiptoLastName(String shiptoLastName) {
        this.shiptoLastName = shiptoLastName;
    }

    public String getShiptoState() {
        return shiptoState;
    }

    public void setShiptoState(String shiptoState) {
        this.shiptoState = shiptoState;
    }

    public String getShiptoZip() {
        return shiptoZip;
    }

    public void setShiptoZip(String shiptoZip) {
        this.shiptoZip = shiptoZip;
    }

    public String getTotal() {
        return total;
    }

    public void setTotal(String total) {
        this.total = total;
    }
}
