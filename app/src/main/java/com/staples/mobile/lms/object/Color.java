package com.staples.mobile.lms.object;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({"title1", "title2", "background1", "color7"})
public class Color {

    private String title1;
    private String title2;
    private String background1;
    private String color7;

    public String getTitle1() {
        return title1;
    }

    public String getTitle2() {
        return title2;
    }

    public String getBackground1() {
        return background1;
    }

    public String getColor7() {
        return color7;
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
