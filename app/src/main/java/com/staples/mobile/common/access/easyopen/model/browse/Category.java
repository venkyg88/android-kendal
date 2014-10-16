package com.staples.mobile.common.access.easyopen.model.browse;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class Category {
    String catalogId;
    List<Analytic> categoryAnalytic;
    String categoryUrl;
    int childCount;
    boolean comparable;
    String comparisonUrl;
    @JsonProperty("description")
    List<Description> description1;
    @JsonProperty("Description")
    List<Description> description2;
    List<FilterGroup> filterGroup;
    String identifier;
    boolean navigable;
    boolean partialResults;
    List<Product> product;
    String recommendationUrl;
    List<SubCategory> subCategory;

    public String getCatalogId() {
        return catalogId;
    }

    public List<Analytic> getCategoryAnalytic() {
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

    public List<Description> getDescription1() {
        return description1;
    }

    public List<Description> getDescription2() {
        return description2;
    }

    public List<FilterGroup> getFilterGroup() {
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

    public List<Product> getProduct() {
        return product;
    }

    public String getRecommendationUrl() {
        return recommendationUrl;
    }

    public List<SubCategory> getSubCategory() {
        return subCategory;
    }
}
