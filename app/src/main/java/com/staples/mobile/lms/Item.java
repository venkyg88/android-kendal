package com.staples.mobile.lms;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Item {
    private String banner;
    private String title;
    @JsonProperty("bundle_id")
    private String bundleId;

    public String getBanner() {
        return banner;
    }

    public String getTitle() {
        return title;
    }

    public String getBundleId() {
        return bundleId;
    }

    public String toString(){
        StringBuilder builder = new StringBuilder();
        builder.append("Item[").append("banner : ").append(banner).append(",\n")
               .append("title : ").append(title).append(",\n")
               .append("bundleId : ").append(bundleId).append(",\n")
               .append("]");
        return builder.toString();
    }
}
