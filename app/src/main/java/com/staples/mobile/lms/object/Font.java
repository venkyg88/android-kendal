package com.staples.mobile.lms.object;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({"text1","text2","text3"})
public class Font {

    private String text1;
    private String text2;
    private String text3;

    @JsonProperty("text1")
    public String getText1(){
        return this.text1;
    }

    public void setText1(String text1){
        this.text1 = text1;
    }

    @JsonProperty("text2")
    public String getText2(){
        return this.text2;
    }

    public void setText2(String text2){
        this.text2 = text2;
    }

    @JsonProperty("text3")
    public String getText3(){
        return this.text3;
    }

    public void setText3(String text3){
        this.text3 = text3;
    }




    public String toString(){
        StringBuilder builder = new StringBuilder();
        builder.append("Font[").append("text1 : ").append(text1).append(",\n")
               .append("text2 : ").append(text2).append(",\n")
               .append("text3 : ").append(text3).append(",\n")
               .append("]");
        return builder.toString();
    }

}
