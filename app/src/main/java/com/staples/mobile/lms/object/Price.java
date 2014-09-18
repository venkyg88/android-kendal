package com.staples.mobile.lms.object;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.lang.StringBuilder;
import java.lang.String;

@JsonPropertyOrder({"extended","list","saving"})
public class Price {

    private String extended;
    private String list;
    private String saving;

    @JsonProperty("extended")
    public String getExtended(){
        return this.extended;
    }

    public void setExtended(String extended){
        this.extended = extended;
    }

    @JsonProperty("list")
    public String getList(){
        return this.list;
    }

    public void setList(String list){
        this.list = list;
    }

    @JsonProperty("saving")
    public String getSaving(){
        return this.saving;
    }

    public void setSaving(String saving){
        this.saving = saving;
    }

    public String toString(){
        StringBuilder builder = new StringBuilder();
        builder.append("Price[").append("extended : ").append(extended).append(",\n")
               .append("list : ").append(list).append(",\n")
               .append("saving : ").append(saving).append(",\n")
               .append("]");
        return builder.toString();
    }
}
