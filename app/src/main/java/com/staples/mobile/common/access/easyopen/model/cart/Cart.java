
package com.staples.mobile.common.access.easyopen.model.cart;

import java.util.ArrayList;
import java.util.List;

public class Cart {

    private String delivery;
    private String orderId;         // returned by /cart
    private String orderIdentifier; // returned by /cart/tax and /cart/shippingCharge
    private float preTaxTotal;
    private List<Product> product = new ArrayList<Product>();
    private String recommendationUrl;
    private float subTotal;
    private int totalItems;
    private boolean webOnly;
    private String shippingCharge;
    private float totalTax;

    public String getDelivery() {
        return delivery;
    }

    public void setDelivery(String delivery) {
        this.delivery = delivery;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getOrderIdentifier() {
        return orderIdentifier;
    }

    public void setOrderIdentifier(String orderIdentifier) {
        this.orderIdentifier = orderIdentifier;
    }

    public float getPreTaxTotal() {
        return preTaxTotal;
    }

    public void setPreTaxTotal(float preTaxTotal) {
        this.preTaxTotal = preTaxTotal;
    }

    public List<Product> getProduct() {
        return product;
    }

    public void setProduct(List<Product> product) {
        this.product = product;
    }

    public String getRecommendationUrl() {
        return recommendationUrl;
    }

    public void setRecommendationUrl(String recommendationUrl) {
        this.recommendationUrl = recommendationUrl;
    }

    public float getSubTotal() {
        return subTotal;
    }

    public void setSubTotal(float subTotal) {
        this.subTotal = subTotal;
    }

    public int getTotalItems() {
        return totalItems;
    }

    public void setTotalItems(int totalItems) {
        this.totalItems = totalItems;
    }

    public boolean getWebOnly() {
        return webOnly;
    }

    public void setWebOnly(boolean webOnly) {
        this.webOnly = webOnly;
    }

    public String getShippingCharge() {
        return shippingCharge;
    }

    public void setShippingCharge(String shippingCharge) {
        this.shippingCharge = shippingCharge;
    }

    public float getTotalTax() {
        return totalTax;
    }

    public void setTotalTax(float totalTax) {
        this.totalTax = totalTax;
    }
}
