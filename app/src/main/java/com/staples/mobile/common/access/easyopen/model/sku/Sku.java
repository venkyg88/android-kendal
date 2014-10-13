package com.staples.mobile.common.access.easyopen.model.sku;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Sku {
    @JsonProperty("Product")
   	private Product[] product;
    private boolean recordSetComplete;
    private int recordSetCount;
    private int recordSetStartNumber;
    private int recordSetTotal;

    public Product[] getProduct() {
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
