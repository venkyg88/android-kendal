
package com.staples.mobile.common.access.easyopen.model.inventory;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

//wrapper class for getStoreInventory() callback
public class StoreInventory {

    @JsonProperty("Store")
    private List<Store> store = new ArrayList<Store>();
    private boolean recordSetComplete;
    private int recordSetCount;
    private int recordSetStartNumber;
    private int recordSetTotal;

    public List<Store> getStore() {
        return store;
    }

    public void setStore(List<Store> store) {
        this.store = store;
    }

    public boolean getRecordSetComplete() {
        return recordSetComplete;
    }

    public void setRecordSetComplete(boolean recordSetComplete) {
        this.recordSetComplete = recordSetComplete;
    }

    public int getRecordSetCount() {
        return recordSetCount;
    }

    public void setRecordSetCount(int recordSetCount) {
        this.recordSetCount = recordSetCount;
    }

    public int getRecordSetStartNumber() {
        return recordSetStartNumber;
    }

    public void setRecordSetStartNumber(int recordSetStartNumber) {
        this.recordSetStartNumber = recordSetStartNumber;
    }

    public int getRecordSetTotal() {
        return recordSetTotal;
    }

    public void setRecordSetTotal(int recordSetTotal) {
        this.recordSetTotal = recordSetTotal;
    }

}
