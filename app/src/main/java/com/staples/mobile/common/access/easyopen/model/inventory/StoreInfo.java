package com.staples.mobile.common.access.easyopen.model.inventory;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

//wrapper class for getting detailed store information

public class StoreInfo {
    @JsonProperty("Store")
    private List<StoreDetails> storeDetails = new ArrayList<StoreDetails>();

    public List<StoreDetails> getStore() {
        return storeDetails;
    }

    public void setStore(List<StoreDetails> store) {
        this.storeDetails = store;
    }

}

