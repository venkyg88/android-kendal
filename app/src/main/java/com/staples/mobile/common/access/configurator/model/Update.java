
package com.staples.mobile.common.access.configurator.model;

import java.util.List;

public class Update {
    private Force force;
    private Suggest suggest;
    private String url;

    public Force getForce() {
        return this.force;
    }

    public void setForce(Force force) {
        this.force = force;
    }

    public Suggest getSuggest() {
        return this.suggest;
    }

    public void setSuggest(Suggest suggest) {
        this.suggest = suggest;
    }

    public String getUrl() {
        return this.url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
