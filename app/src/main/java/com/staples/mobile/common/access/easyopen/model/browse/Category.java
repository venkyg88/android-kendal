package com.staples.mobile.common.access.easyopen.model.browse;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Category {
    private String catalogId;
    private Analytic[] categoryAnalytic;
    private String categoryUrl;
    private int childCount;
    private boolean comparable;
    private String comparisonUrl;
    @JsonProperty("description")
    private Description[] description1;
    @JsonProperty("Description")
    private Description[] description2;
    private FilterGroup filterGroup[];
    private String identifier;
    private boolean navigable;
    private boolean partialResults;
    private Product[] product;
    private String recommendationUrl;
    private SubCategory[] subCategory;

    public String getCatalogId() {
        return catalogId;
    }

    public Analytic[] getCategoryAnalytic() {
        return categoryAnalytic;
    }

    public String getCategoryUrl() {
        return categoryUrl;
    }

    public int getChildCount() {
        return childCount;
    }

    public boolean isComparable() {
        return comparable;
    }

    public String getComparisonUrl() {
        return comparisonUrl;
    }

    public Description[] getDescription1() {
        return description1;
    }

    public Description[] getDescription2() {
        return description2;
    }

    public FilterGroup[] getFilterGroup() {
        return filterGroup;
    }

    public String getIdentifier() {
        return identifier;
    }

    public boolean isNavigable() {
        return navigable;
    }

    public boolean isPartialResults() {
        return partialResults;
    }

    public Product[] getProduct() {
        return product;
    }

    public String getRecommendationUrl() {
        return recommendationUrl;
    }

    public SubCategory[] getSubCategory() {
        return subCategory;
    }
}
