package com.staples.mobile.lms.object;

import java.util.ArrayList;
import java.util.List;

public class Lms {
    private String product;
    private String descriptor;
    private Double build;
    private String releaseDate;
    private String startDate;
    private String endDate;
    private Boolean dev;
    private Style style;
    private Image image;
    private List<Page> page = new ArrayList<Page> ();

    public String getProduct() {
        return product;
    }

    public String getDescriptor() {
        return descriptor;
    }

    public Double getBuild() {
        return build;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public String getStartDate() {
        return startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public Boolean getDev() {
        return dev;
    }

    public Style getStyle() {
        return style;
    }

    public Image getImage() {
        return image;
    }

    public List<Page> getPage() {
        return page;
    }

    public String toString(){
        StringBuilder builder = new StringBuilder();
        builder.append("JsonClass[").append("product : ").append(product).append(",\n")
                .append("descriptor : ").append(descriptor).append(",\n")
                .append("build : ").append(build).append(",\n")
                .append("releaseDate : ").append(releaseDate).append(",\n")
                .append("startDate : ").append(startDate).append(",\n")
                .append("endDate : ").append(endDate).append(",\n")
                .append("dev : ").append(dev).append(",\n")
                .append("style : ").append(style).append(",\n")
                .append("image : ").append(image).append(",\n")
                .append("page : ").append(page).append(",\n")
                .append("]");
        return builder.toString();
    }
}
