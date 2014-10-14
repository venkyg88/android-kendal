
package com.staples.mobile.common.access.easyopen.model.cart;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

public class Product {

    private String bopisEligible;
    private List<BulletDescription> bulletDescription = new ArrayList<BulletDescription>();
    private String classId;
    private String comparable;
    private int customerReviewCount;
    private float customerReviewRating;
    private String deliveryOnly;
    private String expectedBusinessDayDelivery;
    private String freeShipping;
    private List<Image> image = new ArrayList<Image>();
    @JsonProperty("leadTimeDecription")
    private String leadTimeDescription;
    private String manufacturerName;
    private String manufacturerPartNumber;
    private String orderItemDeleteUrl;
    private String orderItemId;
    private String priceInCartOnly;
    private List<Pricing> pricing = new ArrayList<Pricing>();
    private String productName;
    private int quantity;
    private String ropisEligible;
    private String shipableToStore;
    private ShippingInformation shippingInformation;
    private String sku;
    private List<ThumbnailImage> thumbnailImage = new ArrayList<ThumbnailImage>();
    private String uniqueId;
    private String upsable;

    public String getBopisEligible() {
        return bopisEligible;
    }

    public void setBopisEligible(String bopisEligible) {
        this.bopisEligible = bopisEligible;
    }

    public List<BulletDescription> getBulletDescription() {
        return bulletDescription;
    }

    public void setBulletDescription(List<BulletDescription> bulletDescription) {
        this.bulletDescription = bulletDescription;
    }

    public String getClassId() {
        return classId;
    }

    public void setClassId(String classId) {
        this.classId = classId;
    }

    public String getComparable() {
        return comparable;
    }

    public void setComparable(String comparable) {
        this.comparable = comparable;
    }

    public int getCustomerReviewCount() {
        return customerReviewCount;
    }

    public void setCustomerReviewCount(int customerReviewCount) {
        this.customerReviewCount = customerReviewCount;
    }

    public float getCustomerReviewRating() {
        return customerReviewRating;
    }

    public void setCustomerReviewRating(float customerReviewRating) {
        this.customerReviewRating = customerReviewRating;
    }

    public String getDeliveryOnly() {
        return deliveryOnly;
    }

    public void setDeliveryOnly(String deliveryOnly) {
        this.deliveryOnly = deliveryOnly;
    }

    public String getExpectedBusinessDayDelivery() {
        return expectedBusinessDayDelivery;
    }

    public void setExpectedBusinessDayDelivery(String expectedBusinessDayDelivery) {
        this.expectedBusinessDayDelivery = expectedBusinessDayDelivery;
    }

    public String getFreeShipping() {
        return freeShipping;
    }

    public void setFreeShipping(String freeShipping) {
        this.freeShipping = freeShipping;
    }

    public List<Image> getImage() {
        return image;
    }

    public void setImage(List<Image> image) {
        this.image = image;
    }

    public String getLeadTimeDescription() {
        return leadTimeDescription;
    }

    public void setLeadTimeDescription(String leadTimeDescription) {
        this.leadTimeDescription = leadTimeDescription;
    }

    public String getManufacturerName() {
        return manufacturerName;
    }

    public void setManufacturerName(String manufacturerName) {
        this.manufacturerName = manufacturerName;
    }

    public String getManufacturerPartNumber() {
        return manufacturerPartNumber;
    }

    public void setManufacturerPartNumber(String manufacturerPartNumber) {
        this.manufacturerPartNumber = manufacturerPartNumber;
    }

    public String getOrderItemDeleteUrl() {
        return orderItemDeleteUrl;
    }

    public void setOrderItemDeleteUrl(String orderItemDeleteUrl) {
        this.orderItemDeleteUrl = orderItemDeleteUrl;
    }

    public String getOrderItemId() {
        return orderItemId;
    }

    public void setOrderItemId(String orderItemId) {
        this.orderItemId = orderItemId;
    }

    public String getPriceInCartOnly() {
        return priceInCartOnly;
    }

    public void setPriceInCartOnly(String priceInCartOnly) {
        this.priceInCartOnly = priceInCartOnly;
    }

    public List<Pricing> getPricing() {
        return pricing;
    }

    public void setPricing(List<Pricing> pricing) {
        this.pricing = pricing;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getRopisEligible() {
        return ropisEligible;
    }

    public void setRopisEligible(String ropisEligible) {
        this.ropisEligible = ropisEligible;
    }

    public String getShipableToStore() {
        return shipableToStore;
    }

    public void setShipableToStore(String shipableToStore) {
        this.shipableToStore = shipableToStore;
    }

    public ShippingInformation getShippingInformation() {
        return shippingInformation;
    }

    public void setShippingInformation(ShippingInformation shippingInformation) {
        this.shippingInformation = shippingInformation;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public List<ThumbnailImage> getThumbnailImage() {
        return thumbnailImage;
    }

    public void setThumbnailImage(List<ThumbnailImage> thumbnailImage) {
        this.thumbnailImage = thumbnailImage;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public String getUpsable() {
        return upsable;
    }

    public void setUpsable(String upsable) {
        this.upsable = upsable;
    }

}
