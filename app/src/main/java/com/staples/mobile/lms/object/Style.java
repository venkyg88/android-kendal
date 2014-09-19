package com.staples.mobile.lms.object;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({"name","color","font"})
public class Style {

    private String name;
    private Color color;
    private Font font;

    @JsonProperty("name")
    public String getName(){
        return this.name;
    }

    public void setName(String name){
        this.name = name;
    }

    @JsonProperty("color")
    public Color getColor(){
        return this.color;
    }

    public void setColor(Color color){
        this.color = color;
    }

    @JsonProperty("font")
    public Font getFont(){
        return this.font;
    }

    public void setFont(Font font){
        this.font = font;
    }

    public String toString(){
        StringBuilder builder = new StringBuilder();
        builder.append("Style[").append("name : ").append(name).append(",\n")
               .append("color : ").append(color).append(",\n")
               .append("font : ").append(font).append(",\n")
               .append("]");
        return builder.toString();
    }

}
