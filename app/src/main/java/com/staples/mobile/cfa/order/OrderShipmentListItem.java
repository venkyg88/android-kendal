/*
 * Copyright (c) 2015 Staples, Inc. All rights reserved.
 */

package com.staples.mobile.cfa.order;

import com.staples.mobile.common.access.easyopen.model.member.OrderStatus;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * Created by sutdi001 on 3/3/15.
 */
public class OrderShipmentListItem implements Serializable {
    private Date scheduledDeliveryDate;
    private Integer shipmentIndex; // index within order when multiple shipments (e.g. 2 for "Shipment 2" of an order, null if a single shipment)
    private String shipmentNumber;
    private String shipmentStatusDescription;
    private int quantity;
    private Date orderDate;
    private String orderNumber;

    private OrderStatus orderStatus;
    private List<ShipmentSku> skus;

    public OrderShipmentListItem(Integer shipmentIndex, String shipmentNumber, Date scheduledDeliveryDate,
                                 String shipmentStatusDescription, int quantity,
                                 Date orderDate, String orderNumber,
                                 OrderStatus orderStatus,
                                 List<OrderShipmentListItem.ShipmentSku> skus) {
        this.shipmentIndex = shipmentIndex;
        this.shipmentNumber = shipmentNumber;
        this.scheduledDeliveryDate = scheduledDeliveryDate;
        this.shipmentStatusDescription = shipmentStatusDescription;
        this.quantity = quantity;
        this.orderDate = orderDate;
        this.orderNumber = orderNumber;
        this.orderStatus = orderStatus;
        this.skus = skus;
    }

    public Date getScheduledDeliveryDate() {
        return scheduledDeliveryDate;
    }

    public void setScheduledDeliveryDate(Date scheduledDeliveryDate) {
        this.scheduledDeliveryDate = scheduledDeliveryDate;
    }

    public Integer getShipmentIndex() {
        return shipmentIndex;
    }

    public void setShipmentIndex(Integer shipmentIndex) {
        this.shipmentIndex = shipmentIndex;
    }

    public String getShipmentNumber() {
        return shipmentNumber;
    }

    public void setShipmentNumber(String shipmentNumber) {
        this.shipmentNumber = shipmentNumber;
    }

    public String getShipmentStatusDescription() {
        return shipmentStatusDescription;
    }

    public void setShipmentStatusDescription(String shipmentStatusDescription) {
        this.shipmentStatusDescription = shipmentStatusDescription;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public Date getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(Date orderDate) {
        this.orderDate = orderDate;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }



    public OrderStatus getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(OrderStatus orderStatus) {
        this.orderStatus = orderStatus;
    }

    public List<ShipmentSku> getSkus() {
        return skus;
    }

    public void setSkus(List<ShipmentSku> skus) {
        this.skus = skus;
    }


    public static class ShipmentSku implements Serializable {
        private String skuDescription;
        private String skuNumber;
        private float lineTotal;
        private int qtyOrdered;

        public ShipmentSku(String skuNumber, String skuDescription, int qtyOrdered, float lineTotal) {
            this.skuNumber = skuNumber;
            this.skuDescription = skuDescription;
            this.qtyOrdered = qtyOrdered;
            this.lineTotal = lineTotal;
        }

        public String getSkuDescription() {
            return skuDescription;
        }

        public void setSkuDescription(String skuDescription) {
            this.skuDescription = skuDescription;
        }

        public String getSkuNumber() {
            return skuNumber;
        }

        public void setSkuNumber(String skuNumber) {
            this.skuNumber = skuNumber;
        }

        public float getLineTotal() {
            return lineTotal;
        }

        public void setLineTotal(float lineTotal) {
            this.lineTotal = lineTotal;
        }

        public int getQtyOrdered() {
            return qtyOrdered;
        }

        public void setQtyOrdered(int qtyOrdered) {
            this.qtyOrdered = qtyOrdered;
        }
    }
}
