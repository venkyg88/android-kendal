package com.staples.mobile.common.access.easyopen.model.browse;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class SkuDetails {
    @JsonProperty("Product")
    private List<Product> product;
    private boolean recordSetComplete;
    private int recordSetCount;
    private int recordSetStartNumber;
    private int recordSetTotal;

    public List<Product> getProduct() {
        return product;
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
}
