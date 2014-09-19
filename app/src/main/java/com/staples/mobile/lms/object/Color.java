package com.staples.mobile.lms.object;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({"title1","title2","background1","color7"})
public class Color {

    private String title1;
    private String title2;
    private String background1;
    private String color7;

    @JsonProperty("title1")
    public String getTitle1(){
        return this.title1;
    }

    public void setTitle1(String title1){
        this.title1 = title1;
    }

    @JsonProperty("title2")
    public String getTitle2(){
        return this.title2;
    }

    public void setTitle2(String title2){
        this.title2 = title2;
    }

    @JsonProperty("background1")
    public String getBackground1(){
        return this.background1;
    }

    public void setBackground1(String background1){
        this.background1 = background1;
    }

    @JsonProperty("color7")
    public String getColor7(){
        return this.color7;
    }

    public void setColor7(String color7){
        this.color7 = color7;
    }




    public String toString(){
        StringBuilder builder = new StringBuilder();
        builder.append("Color[").append("title1 : ").append(title1).append(",\n")
               .append("title2 : ").append(title2).append(",\n")
               .append("background1 : ").append(background1).append(",\n")
               .append("color7 : ").append(color7).append(",\n")
               .append("]");
        return builder.toString();
    }

}
