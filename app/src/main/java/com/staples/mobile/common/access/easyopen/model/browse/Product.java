package com.staples.mobile.common.access.easyopen.model.browse;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class Product {
    List<Product> accessory;
    List<Product> alsoConsider;
    List<Analytic> analytic;
    boolean bopisEligible;
    List<BulletDescription> bulletDescription;
    boolean businessService;
    String classId;
    boolean comparable;
    List<Description> customTab;
    int customerReviewCount;
    float customerReviewRating;
    boolean deliveryOnly;
    String displayName;
    boolean esd;
    List<Description> expandedText;
    boolean freeShipping;
    List<Description> headliner;
    List<Image> image;
    boolean inStock;
    @JsonProperty("leadTimeDecription")
    String leadTimeDescription;
    int leadTimeMaximum;
    int leadTimeMinimum;
    String manufacturerName;
    String manufacturerPartNumber;
    List<Description> paragraph;
    boolean priceInCartOnly;
    List<Pricing> pricing;
    List<Product> product;
    String productName;
    String productUrl;
    List<Description> promotionalOfferExpire;
    String recommendationUrl;
    boolean registeredUserOnly;
    boolean retailOnly;
    boolean retailOnlySpecialOrder;
    boolean ropisEligible;
    int ropisQtyLimit;
    float savings;
    boolean shipableToStore;
    boolean shopRunner;
    String sku;
    List<Image> smallPromoImage;
    List<Description> specification;
    String staplesDisplayPartNumber;
    String supplierTypeId;
    List<Description> termsAndCondition;
    List<ThumbnailImage> thumbnailImage;
    String uniqueId;
    boolean upsable;
    boolean webOnly;

    public List<Product> getAccessory() {
        return accessory;
    }

    public List<Product> getAlsoConsider() {
        return alsoConsider;
    }

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

    public List<Description> getCustomTab() {
        return customTab;
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

    public boolean isEsd() {
        return esd;
    }

    public List<Description> getExpandedText() {
        return expandedText;
    }

    public boolean isFreeShipping() {
        return freeShipping;
    }

    public List<Description> getHeadliner() {
        return headliner;
    }

    public List<Image> getImage() {
        return image;
    }

    public boolean isInStock() {
        return inStock;
    }

    public String getLeadTimeDescription() {
        return leadTimeDescription;
    }

    public int getLeadTimeMaximum() {
        return leadTimeMaximum;
    }

    public int getLeadTimeMinimum() {
        return leadTimeMinimum;
    }

    public String getManufacturerName() {
        return manufacturerName;
    }

    public String getManufacturerPartNumber() {
        return manufacturerPartNumber;
    }

    public List<Description> getParagraph() {
        return paragraph;
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

    public boolean isRegisteredUserOnly() {
        return registeredUserOnly;
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

    public List<Description> getSpecification() {
        return specification;
    }

    public String getStaplesDisplayPartNumber() {
        return staplesDisplayPartNumber;
    }

    public String getSupplierTypeId() {
        return supplierTypeId;
    }

    public List<Description> getTermsAndCondition() {
        return termsAndCondition;
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
