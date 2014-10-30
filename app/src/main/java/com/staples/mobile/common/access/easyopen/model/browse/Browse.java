package com.staples.mobile.common.access.easyopen.model.browse;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class Browse {
    @JsonProperty("Category")
    private List<Category> category;
    private String recommendationUrl;
    private boolean recordSetComplete;
    private int recordSetCount;
    private int recordSetStartNumber;
    private int recordSetTotal;
    private String resourceUrl;

    public List<Category> getCategory() {
        return category;
    }

    public String getRecommendationUrl() {
        return recommendationUrl;
    }

    public boolean isRecordSetComplete() {
        return recordSetComplete;
    }

    public int getRecordSetCount() {
        return recordSetCount;
    }

    public int getRecordSetStartNumber() {
        return recordSetStartNumber;
    }

    public int getRecordSetTotal() {
        return recordSetTotal;
    }

    public String getResourceUrl() {
        return resourceUrl;
    }
}
