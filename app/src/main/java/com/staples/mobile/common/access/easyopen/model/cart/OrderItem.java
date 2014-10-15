package com.staples.mobile.common.access.easyopen.model.cart;

public class OrderItem {
    private OrderItemId orderItemId;
    private String partNumber_X;
    private int quantity_x;

    public OrderItemId getOrderItemId() {
        return orderItemId;
    }

    public void setOrderItemId(OrderItemId orderItemId) {
        this.orderItemId = orderItemId;
    }

    public String getPartNumber_X() {
        return partNumber_X;
    }

    public void setPartNumber_X(String partNumber_X) {
        this.partNumber_X = partNumber_X;
    }

    public int getQuantity_x() {
        return quantity_x;
    }

    public void setQuantity_x(int quantity_x) {
        this.quantity_x = quantity_x;
    }
}
