package com.staples.mobile.lms.object;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.lang.StringBuilder;
import java.lang.String;

@JsonPropertyOrder({"page","item"})
public class Pattern {

    private String page;
    private String item;

    @JsonProperty("page")
    public String getPage(){
        return this.page;
    }

    public void setPage(String page){
        this.page = page;
    }

    @JsonProperty("item")
    public String getItem(){
        return this.item;
    }

    public void setItem(String item){
        this.item = item;
    }




    public String toString(){
        StringBuilder builder = new StringBuilder();
        builder.append("Pattern[").append("page : ").append(page).append(",\n")
               .append("item : ").append(item).append(",\n")
               .append("]");
        return builder.toString();
    }

}
