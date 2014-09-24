package com.staples.mobile.easyopen;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by pyhre001 on 9/10/14.
 */
public class Category {
    private String catalogId;
    private CategoryAnalytic[] categoryAnalytic;
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

    public CategoryAnalytic[] getCategoryAnalytic() {
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
