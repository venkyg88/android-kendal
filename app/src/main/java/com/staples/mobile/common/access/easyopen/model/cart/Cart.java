
package com.staples.mobile.common.access.easyopen.model.cart;

import java.util.ArrayList;
import java.util.List;

public class Cart {

    private String delivery;
    private String orderId;
    private String preTaxTotal;
    private List<Product> product = new ArrayList<Product>();
    private String recommendationUrl;
    private String subTotal;
    private String totalItems;
    private String webOnly;

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

    public String getPreTaxTotal() {
        return preTaxTotal;
    }

    public void setPreTaxTotal(String preTaxTotal) {
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

    public String getSubTotal() {
        return subTotal;
    }

    public void setSubTotal(String subTotal) {
        this.subTotal = subTotal;
    }

    public String getTotalItems() {
        return totalItems;
    }

    public void setTotalItems(String totalItems) {
        this.totalItems = totalItems;
    }

    public String getWebOnly() {
        return webOnly;
    }

    public void setWebOnly(String webOnly) {
        this.webOnly = webOnly;
    }
}
