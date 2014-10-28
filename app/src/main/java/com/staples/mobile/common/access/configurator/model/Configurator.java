
package com.staples.mobile.common.access.configurator.model;

import java.util.List;

public class Configurator {
    private List<Api> api;
    private String descriptor;
    private boolean dev;
    private String endDate;
    private Pow pow;
    private String product;
    private Promotions promotions;
    private String releaseDate;
    private List<Screen> screen;
    private String startDate;
    private Update update;
    private Number version;

    public List<Api> getApi() {
        return this.api;
    }

    public void setApi(List<Api> api) {
        this.api = api;
    }

    public String getDescriptor() {
        return this.descriptor;
    }

    public void setDescriptor(String descriptor) {
        this.descriptor = descriptor;
    }

    public boolean getDev() {
        return this.dev;
    }

    public void setDev(boolean dev) {
        this.dev = dev;
    }

    public String getEndDate() {
        return this.endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public Pow getPow() {
        return this.pow;
    }

    public void setPow(Pow pow) {
        this.pow = pow;
    }

    public String getProduct() {
        return this.product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public Promotions getPromotions() {
        return this.promotions;
    }

    public void setPromotions(Promotions promotions) {
        this.promotions = promotions;
    }

    public String getReleaseDate() {
        return this.releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public List<Screen> getScreen() {
        return this.screen;
    }

    public void setScreen(List<Screen> screen) {
        this.screen = screen;
    }

    public String getStartDate() {
        return this.startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public Update getUpdate() {
        return this.update;
    }

    public void setUpdate(Update update) {
        this.update = update;
    }

    public Number getVersion() {
        return this.version;
    }

    public void setVersion(Number version) {
        this.version = version;
    }
}
