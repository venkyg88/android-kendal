package com.staples.mobile.cfa.order;

import com.staples.mobile.common.access.easyopen.model.member.OrderStatus;
import com.staples.mobile.common.access.easyopen.model.member.Shipment;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OrderShipmentListItem implements Serializable, Comparable<OrderShipmentListItem> {
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("EE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH);

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

    public static Date parseDate(String date) {
        try {
            return dateFormat.parse(date);
        } catch (ParseException e) {
            return new Date(0);
        }
    }

    @Override
    public int compareTo(OrderShipmentListItem another) {
        int result = 0;
        // first sort by descending order date to make sure most recent are shown at the top
        Date leftParsedDate = OrderShipmentListItem.parseDate(getOrderStatus().getOrderDate());
        Date rightParsedDate = OrderShipmentListItem.parseDate(another.getOrderStatus().getOrderDate());
        result = rightParsedDate.compareTo(leftParsedDate);
        if (result == 0) {
            // next sort by descending order number to make sure shipments of an order are grouped together
            result = another.getOrderStatus().getOrderNumber().compareTo(getOrderStatus().getOrderNumber());
            if (result == 0) {
                // next sort by delivery date
                result = another.getShipment().getScheduledDeliveryDate().compareTo(getShipment().getScheduledDeliveryDate());
                if (result == 0) {
                    // next sort by shipment index
                    result = getShipmentIndex() - another.getShipmentIndex();
                }
            }
        }
        return result;
    }
}
