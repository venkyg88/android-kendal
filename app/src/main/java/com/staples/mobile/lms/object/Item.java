package com.staples.mobile.lms.object;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({"banner","title","bundleId"})
public class Item {

    private String banner;
    private String title;
    private String bundleId;

    @JsonProperty("banner")
    public String getBanner(){
        return this.banner;
    }

    public void setBanner(String banner){
        this.banner = banner;
    }

    @JsonProperty("title")
    public String getTitle(){
        return this.title;
    }

    public void setTitle(String title){
        this.title = title;
    }

    @JsonProperty("bundle_id")
    public String getBundleId(){
        return this.bundleId;
    }

    public void setBundleId(String bundleId){
        this.bundleId = bundleId;
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
