package com.staples.mobile.common.access.easyopen.model.browse;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class Product {
    private List<Product> accessory;
    private boolean addOn;
    private List<Product> alsoConsider;
    private List<Analytic> analytic;
    private boolean apple;
    private boolean bopisEligible;
    private List<BulletDescription> bulletDescription;
    private boolean businessService;
    private String classId;
    private boolean comparable;
    private List<Description> customTab;
    private int customerReviewCount;
    private float customerReviewRating;
    private boolean deliveryOnly;
    private String displayName;
    private boolean esd;
    private List<Description> expandedText;
    private boolean freeShipping;
    private List<Description> headliner;
    private boolean heavyWeightSKU;
    private List<Image> image;
    private boolean inStock;
    @JsonProperty("leadTimeDecription")
    private String leadTimeDescription;
    private int leadTimeMaximum;
    private int leadTimeMinimum;
    private String manufacturerName;
    private String manufacturerPartNumber;
    private List<Description> paragraph;
    private boolean preOrderFlag;
    private boolean priceInCartOnly;
    private List<Pricing> pricing;
    private List<Product> product;
    private String productName;
    private String productUrl;
    private List<Description> promotionalOfferExpire;
    private String recommendationUrl;
    private boolean registeredUserOnly;
    private boolean retailOnly;
    private boolean retailOnlySpecialOrder;
    private boolean ropisEligible;
    private int ropisQtyLimit;
    private float savings;
    private boolean shipableToStore;
    private boolean shopRunner;
    private String sku;
    private List<Image> smallInfoImage;
    private List<Image> smallPromoImage;
    private List<Description> specification;
    private String staplesDisplayPartNumber;
    private String supplierTypeId;
    private List<Description> termsAndCondition;
    private List<Image> thumbnailImage;
    private String uniqueId;
    private boolean upsable;
    private boolean webOnly;

    public List<Product> getAccessory() {
        return accessory;
    }

    public boolean isAddOn() {
        return addOn;
    }

    public List<Product> getAlsoConsider() {
        return alsoConsider;
    }

    public List<Analytic> getAnalytic() {
        return analytic;
    }

    public boolean isApple() {
        return apple;
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

    public boolean isHeavyWeightSKU() {
        return heavyWeightSKU;
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

    public boolean isPreOrderFlag() {
        return preOrderFlag;
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

    public List<Image> getSmallInfoImage() {
        return smallInfoImage;
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

    public List<Image> getThumbnailImage() {
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