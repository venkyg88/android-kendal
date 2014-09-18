package com.staples.mobile.lms.object;

import java.util.ArrayList;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.lang.StringBuilder;

@JsonPropertyOrder({"item"})
public class ItemPatternDetails {

    private List<Item> item = new ArrayList<Item> ();

    @JsonProperty("item")
    public List<Item> getItem(){
        return this.item;
    }

    public void setItem(List<Item> item){
        this.item = item;
    }




    public String toString(){
        StringBuilder builder = new StringBuilder();
        builder.append("ItemPatternDetails[").append("item : ").append(item).append(",\n")
               .append("]");
        return builder.toString();
    }

}
