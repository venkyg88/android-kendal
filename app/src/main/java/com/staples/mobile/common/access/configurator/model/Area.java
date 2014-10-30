package com.staples.mobile.common.access.configurator.model;

import java.util.List;

public class Area {
    private String skuList;
    private List<String> skuItem;

    public String getSkuList() {
        return this.skuList;
    }

    public void setSkuList(String skuList) {
        this.skuList = skuList;
    }

    public List<String> getSkuItem() {
        return this.skuItem;
    }

    public void setSkuItem(List<String> skuItem) {
        this.skuItem = skuItem;
    }
}
