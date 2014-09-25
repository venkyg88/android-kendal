package com.staples.mobile.common.access.lms.model;

import java.util.ArrayList;
import java.util.List;

public class FormFactor {
    private String landscape;
    private String small;
    private String medium;
    private String large;
    private String xlarge;
    private List<Item> item = new ArrayList<Item> ();

    public String getLandscape() {
        return landscape;
    }

    public String getSmall() {
        return small;
    }

    public String getMedium() {
        return medium;
    }

    public String getLarge() {
        return large;
    }

    public String getXlarge() {
        return xlarge;
    }

    public List<Item> getItem() {
        return item;
    }

    public String toString(){
        StringBuilder builder = new StringBuilder();
        builder.append("FormFactor[").append("landscape : ").append(landscape).append(",\n")
                .append("small : ").append(small).append(",\n")
                .append("medium : ").append(medium).append(",\n")
                .append("large : ").append(large).append(",\n")
                .append("xlarge : ").append(xlarge).append(",\n")
                .append("item : ").append(item).append(",\n")
                .append("]");
        return builder.toString();
    }
}
