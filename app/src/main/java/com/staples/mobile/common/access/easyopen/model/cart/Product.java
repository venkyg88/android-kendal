
package com.staples.mobile.common.access.easyopen.model.cart;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

//Note that this class has different fields than browse.Product and sku.Product
public class Product {

    private boolean bopisEligible;
    private List<BulletDescription> bulletDescription = new ArrayList<BulletDescription>();
    private String classId;
    private boolean comparable;
    private int customerReviewCount;
    private float customerReviewRating;
    private boolean deliveryOnly;
    private String expectedBusinessDayDelivery;
    private boolean freeShipping;
    private List<Image> image = new ArrayList<Image>();
    @JsonProperty("leadTimeDecription")
    private String leadTimeDescription;
    private String manufacturerName;
    private String manufacturerPartNumber;
    private String orderItemDeleteUrl;
    private String orderItemId;
    private boolean priceInCartOnly;
    private List<Pricing> pricing = new ArrayList<Pricing>();
    private String productName;
    private List<PromotionalOfferExpire> promotionalOfferExpire;
    private int quantity;
    private boolean ropisEligible;
    private boolean shipableToStore;
    private ShippingInformation shippingInformation;
    private String sku;
    private List<Image> smallInfoImage;
    private List<Image> smallPromoImage;
    private List<Image> thumbnailImage = new ArrayList<Image>();
    private String uniqueId;
    private boolean upsable;

    public boolean getBopisEligible() {
        return bopisEligible;
    }

    public void setBopisEligible(boolean bopisEligible) {
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

    public boolean getComparable() {
        return comparable;
    }

    public void setComparable(boolean comparable) {
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

    public boolean getDeliveryOnly() {
        return deliveryOnly;
    }

    public void setDeliveryOnly(boolean deliveryOnly) {
        this.deliveryOnly = deliveryOnly;
    }

    public String getExpectedBusinessDayDelivery() {
        return expectedBusinessDayDelivery;
    }

    public void setExpectedBusinessDayDelivery(String expectedBusinessDayDelivery) {
        this.expectedBusinessDayDelivery = expectedBusinessDayDelivery;
    }

    public boolean getFreeShipping() {
        return freeShipping;
    }

    public void setFreeShipping(boolean freeShipping) {
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

    public boolean getPriceInCartOnly() {
        return priceInCartOnly;
    }

    public void setPriceInCartOnly(boolean priceInCartOnly) {
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

    public boolean getRopisEligible() {
        return ropisEligible;
    }

    public void setRopisEligible(boolean ropisEligible) {
        this.ropisEligible = ropisEligible;
    }

    public boolean getShipableToStore() {
        return shipableToStore;
    }

    public void setShipableToStore(boolean shipableToStore) {
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

    public List<Image> getThumbnailImage() {
        return thumbnailImage;
    }

    public void setThumbnailImage(List<Image> thumbnailImage) {
        this.thumbnailImage = thumbnailImage;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public boolean getUpsable() {
        return upsable;
    }

    public void setUpsable(boolean upsable) {
        this.upsable = upsable;
    }

    public List<Image> getSmallInfoImage() {
        return smallInfoImage;
    }

    public void setSmallInfoImage(List<Image> smallInfoImage) {
        this.smallInfoImage = smallInfoImage;
    }

    public List<PromotionalOfferExpire> getPromotionalOfferExpire() {
        return promotionalOfferExpire;
    }

    public void setPromotionalOfferExpire(List<PromotionalOfferExpire> promotionalOfferExpire) {
        this.promotionalOfferExpire = promotionalOfferExpire;
    }

    public List<Image> getSmallPromoImage() {
        return smallPromoImage;
    }

    public void setSmallPromoImage(List<Image> smallPromoImage) {
        this.smallPromoImage = smallPromoImage;
    }
}
