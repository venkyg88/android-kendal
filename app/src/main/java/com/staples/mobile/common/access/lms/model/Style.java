package com.staples.mobile.common.access.lms.model;

public class Style {
    private String name;
    private Color color;
    private Font font;

    public String getName() {
        return name;
    }

    public Color getColor() {
        return color;
    }

    public Font getFont() {
        return font;
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
