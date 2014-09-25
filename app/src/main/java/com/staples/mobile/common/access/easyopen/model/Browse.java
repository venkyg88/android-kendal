package com.staples.mobile.common.access.easyopen.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by pyhre001 on 9/10/14.
 */
public class Browse {
    @JsonProperty("Category")
    private Category[] category;
    private String recommendationUrl;
    private boolean recordSetComplete;
    private int recordSetCount;
    private int recordSetStartNumber;
    private int recordSetTotal;
    private String resourceUrl;

    public Category[] getCategory() {
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
