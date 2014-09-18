package com.staples.mobile.lms.object;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.lang.StringBuilder;

@JsonPropertyOrder({"pattern","formFactor","itemPatternDetails"})
public class Landing {

    private Pattern pattern;
    private FormFactor formFactor;
    private ItemPatternDetails itemPatternDetails;

    @JsonProperty("pattern")
    public Pattern getPattern(){
        return this.pattern;
    }

    public void setPattern(Pattern pattern){
        this.pattern = pattern;
    }

    @JsonProperty("form_factor")
    public FormFactor getFormFactor(){
        return this.formFactor;
    }

    public void setFormFactor(FormFactor formFactor){
        this.formFactor = formFactor;
    }

    @JsonProperty("item_pattern_details")
    public ItemPatternDetails getItemPatternDetails(){
        return this.itemPatternDetails;
    }

    public void setItemPatternDetails(ItemPatternDetails itemPatternDetails){
        this.itemPatternDetails = itemPatternDetails;
    }




    public String toString(){
        StringBuilder builder = new StringBuilder();
        builder.append("Landing[").append("pattern : ").append(pattern).append(",\n")
               .append("formFactor : ").append(formFactor).append(",\n")
               .append("itemPatternDetails : ").append(itemPatternDetails).append(",\n")
               .append("]");
        return builder.toString();
    }

}
