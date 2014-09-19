package com.staples.mobile.lms.object;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.ArrayList;
import java.util.List;

@JsonPropertyOrder({"landscape","small","medium","large","xlarge","item"})
public class FormFactor {

    private String landscape;
    private String small;
    private String medium;
    private String large;
    private String xlarge;
    private List<Item> item = new ArrayList<Item> ();

    @JsonProperty("landscape")
    public String getLandscape(){
        return this.landscape;
    }

    public void setLandscape(String landscape){
        this.landscape = landscape;
    }

    @JsonProperty("small")
    public String getSmall(){
        return this.small;
    }

    public void setSmall(String small){
        this.small = small;
    }

    @JsonProperty("medium")
    public String getMedium(){
        return this.medium;
    }

    public void setMedium(String medium){
        this.medium = medium;
    }

    @JsonProperty("large")
    public String getLarge(){
        return this.large;
    }

    public void setLarge(String large){
        this.large = large;
    }

    @JsonProperty("xlarge")
    public String getXlarge(){
        return this.xlarge;
    }

    public void setXlarge(String xlarge){
        this.xlarge = xlarge;
    }

    @JsonProperty("item")
    public List<Item> getItem(){
        return this.item;
    }

    public void setItem(List<Item> item){
        this.item = item;
    }




    public String toString(){
        StringBuilder builder = new StringBuilder();
        builder.append("FormFactor[").append("landscape : ").append(landscape).append(",\n")
               .append("small : ").append(small).append(",\n")
               .append("medium : ").append(medium).append(",\n")
               .append("large : ").append(large).append(",\n")
               .append("xlarge : ").append(xlarge).append(",\n")
               .append("item : ").append(item).append(",\n")
               .append("]");
        return builder.toString();
    }

}
