package com.staples.mobile.lms.object;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

//@JsonPropertyOrder({"name","pattern","formFactor"})
public class Page {

    private String name;
    private String pattern;
    @JsonProperty("form_factor")
    private FormFactor formFactor;

    @JsonProperty("name")
    public String getName(){
        return this.name;
    }

    public void setName(String name){
        this.name = name;
    }

    @JsonProperty("pattern")
    public String getPattern(){
        return this.pattern;
    }

    public void setPattern(String pattern){
        this.pattern = pattern;
    }

    @JsonProperty("form_factor")
    public FormFactor getFormFactor(){
        return this.formFactor;
    }

    @JsonProperty("form_factor")
    public void setFormFactor(FormFactor formFactor){
        this.formFactor = formFactor;
    }

    public String toString(){
        StringBuilder builder = new StringBuilder();
        builder.append("Page[").append("name : ").append(name).append(",\n")
               .append("pattern : ").append(pattern).append(",\n")
               .append("formFactor : ").append(formFactor).append(",\n")
               .append("]");
        return builder.toString();
    }

}
