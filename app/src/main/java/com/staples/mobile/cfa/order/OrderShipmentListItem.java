/*
 * Copyright (c) 2015 Staples, Inc. All rights reserved.
 */

package com.staples.mobile.cfa.order;

import com.staples.mobile.common.access.easyopen.model.member.OrderStatus;
import com.staples.mobile.common.access.easyopen.model.member.Shipment;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by sutdi001 on 3/3/15.
 */
public class OrderShipmentListItem implements Serializable {
    private int shipmentIndex; // zero-based index of list item's shipment within order
    private OrderStatus orderStatus;

    public OrderShipmentListItem(int shipmentIndex, OrderStatus orderStatus) {
        this.shipmentIndex = shipmentIndex;
        this.orderStatus = orderStatus;
    }

    public List<Shipment> getShipments() {
        return orderStatus.getShipment();
    }

    public Shipment getShipment() {
        return orderStatus.getShipment().get(shipmentIndex);
    }

    public int getShipmentIndex() {
        return shipmentIndex;
    }

    public void setShipmentIndex(int shipmentIndex) {
        this.shipmentIndex = shipmentIndex;
    }

    public OrderStatus getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(OrderStatus orderStatus) {
        this.orderStatus = orderStatus;
    }

    private static SimpleDateFormat dateFormat = new SimpleDateFormat("EE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH);
    public static Date parseDate(String date) {
        try {
            return dateFormat.parse(date);
        } catch (ParseException e) {
            // return oldest possible date
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(0);
            return cal.getTime();
        }
    }
}
