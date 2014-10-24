package com.staples.mobile.common.access.easyopen.model.search;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class Product {
    List<Analytic> analytic;
    boolean bopisEligible;
    List<BulletDescription> bulletDescription;
    boolean businessService;
    String classId;
    boolean comparable;
    int customerReviewCount;
    float customerReviewRating;
    boolean deliveryOnly;
    String displayName;
    boolean freeShipping;
    List<Image> image;
    @JsonProperty("leadTimeDecription")
    String leadTimeDescription;
    String manufacturerName;
    String manufacturerPartNumber;
    boolean priceInCartOnly;
    List<Pricing> pricing;
    List<Product> product;
    String productName;
    String productUrl;
    List<Description> promotionalOfferExpire;
    String recommendationUrl;
    boolean retailOnly;
    boolean retailOnlySpecialOrder;
    boolean ropisEligible;
    int ropisQtyLimit;
    float savings;
    boolean shipableToStore;
    boolean shopRunner;
    String sku;
    List<Image> smallPromoImage;
    String staplesDisplayPartNumber;
    String supplierTypeId;
    List<ThumbnailImage> thumbnailImage;
    String uniqueId;
    boolean upsable;
    boolean webOnly;

    public List<Analytic> getAnalytic() {
        return analytic;
    }

    public boolean isBopisEligible() {
        return bopisEligible;
    }

    public List<BulletDescription> getBulletDescription() {
        return bulletDescription;
    }

    public boolean isBusinessService() {
        return businessService;
    }

    public String getClassId() {
        return classId;
    }

    public boolean isComparable() {
        return comparable;
    }

    public int getCustomerReviewCount() {
        return customerReviewCount;
    }

    public float getCustomerReviewRating() {
        return customerReviewRating;
    }

    public boolean isDeliveryOnly() {
        return deliveryOnly;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isFreeShipping() {
        return freeShipping;
    }

    public List<Image> getImage() {
        return image;
    }

    public String getLeadTimeDescription() {
        return leadTimeDescription;
    }

    public String getManufacturerName() {
        return manufacturerName;
    }

    public String getManufacturerPartNumber() {
        return manufacturerPartNumber;
    }

    public boolean isPriceInCartOnly() {
        return priceInCartOnly;
    }

    public List<Pricing> getPricing() {
        return pricing;
    }

    public List<Product> getProduct() {
        return product;
    }

    public String getProductName() {
        return productName;
    }

    public String getProductUrl() {
        return productUrl;
    }

    public List<Description> getPromotionalOfferExpire() {
        return promotionalOfferExpire;
    }

    public String getRecommendationUrl() {
        return recommendationUrl;
    }

    public boolean isRetailOnly() {
        return retailOnly;
    }

    public boolean isRetailOnlySpecialOrder() {
        return retailOnlySpecialOrder;
    }

    public boolean isRopisEligible() {
        return ropisEligible;
    }

    public int getRopisQtyLimit() {
        return ropisQtyLimit;
    }

    public float getSavings() {
        return savings;
    }

    public boolean isShipableToStore() {
        return shipableToStore;
    }

    public boolean isShopRunner() {
        return shopRunner;
    }

    public String getSku() {
        return sku;
    }

    public List<Image> getSmallPromoImage() {
        return smallPromoImage;
    }

    public String getStaplesDisplayPartNumber() {
        return staplesDisplayPartNumber;
    }

    public String getSupplierTypeId() {
        return supplierTypeId;
    }

    public List<ThumbnailImage> getThumbnailImage() {
        return thumbnailImage;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public boolean isUpsable() {
        return upsable;
    }

    public boolean isWebOnly() {
        return webOnly;
    }
}
