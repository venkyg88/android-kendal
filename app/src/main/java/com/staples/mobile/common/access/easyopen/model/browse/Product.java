package com.staples.mobile.common.access.easyopen.model.browse;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by pyhre001 on 9/22/14.
 */
public class Product {
    private boolean bopisEligible;
    private BulletDescription[] bulletDescription;
    private String classId;
    private boolean comparable;
    private int customerReviewCount;
    private float customerReviewRating;
    private boolean deliveryOnly;
    private String displayName;
    private boolean freeShipping;
    private Image[] image;
    @JsonProperty("leadTimeDecription")
    private String leadTimeDescription;
    private String manufacturerName;
    private String manufacturerPartNumber;
    private boolean priceInCartOnly;
    private Pricing[] pricing;
    private String productName;
    private String productUrl;
    private Description[] promotionalOfferExpire;
    private boolean retailOnly;
    private boolean retailOnlySpecialOrder;
    private boolean ropisEligible;
    private int ropisQtyLimit;
    private float savings;
    private boolean shipableToStore;
    private String sku;
    private Image[] smallPromoImage;
    private String staplesDisplayPartNumber;
    private ThumbnailImage[] thumbnailImage;
    private String uniqueId;
    private boolean upsable;
    private boolean webOnly;

    public boolean isBopisEligible() {
        return bopisEligible;
    }

    public BulletDescription[] getBulletDescription() {
        return bulletDescription;
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

    public Image[] getImage() {
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

    public Pricing[] getPricing() {
        return pricing;
    }

    public String getProductName() {
        return productName;
    }

    public String getProductUrl() {
        return productUrl;
    }

    public Description[] getPromotionalOfferExpire() {
        return promotionalOfferExpire;
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

    public String getSku() {
        return sku;
    }

    public Image[] getSmallPromoImage() {
        return smallPromoImage;
    }

    public String getStaplesDisplayPartNumber() {
        return staplesDisplayPartNumber;
    }

    public ThumbnailImage[] getThumbnailImage() {
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
