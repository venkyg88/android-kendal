package com.staples.mobile.common.access.easyopen.model.browse;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class Browse {
    @JsonProperty("Category")
    List<Category> category;
    String recommendationUrl;
    boolean recordSetComplete;
    int recordSetCount;
    int recordSetStartNumber;
    int recordSetTotal;
    String resourceUrl;

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
