
package com.staples.mobile.common.access.configurator.model;

import java.util.List;

public class Item {
    private List<Area> area;
    private String banner;
    private String size;
    private String title;

    public List<Area> getArea() {
        return this.area;
    }

    public void setArea(List<Area> area) {
        this.area = area;
    }

    public String getBanner() {
        return this.banner;
    }

    public void setBanner(String banner) {
        this.banner = banner;
    }

    public String getSize() {
        return this.size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
