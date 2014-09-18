package com.staples.mobile.lms.object;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({"product","descriptor","build","releaseDate","startDate","endDate","dev","images","pages"})
public class Lms {

    private String product;
    private String descriptor;
    private Double build;
    private String releaseDate;
    private String startDate;
    private String endDate;
    private Boolean dev;
    private Images images;
    private Pages pages;

    @JsonProperty("product")
    public String getProduct(){
        return this.product;
    }

    public void setProduct(String product){
        this.product = product;
    }

    @JsonProperty("descriptor")
    public String getDescriptor(){
        return this.descriptor;
    }

    public void setDescriptor(String descriptor){
        this.descriptor = descriptor;
    }

    @JsonProperty("build")
    public Double getBuild(){
        return this.build;
    }

    public void setBuild(Double build){
        this.build = build;
    }

    @JsonProperty("releaseDate")
    public String getReleaseDate(){
        return this.releaseDate;
    }

    public void setReleaseDate(String releaseDate){
        this.releaseDate = releaseDate;
    }

    @JsonProperty("startDate")
    public String getStartDate(){
        return this.startDate;
    }

    public void setStartDate(String startDate){
        this.startDate = startDate;
    }

    @JsonProperty("endDate")
    public String getEndDate(){
        return this.endDate;
    }

    public void setEndDate(String endDate){
        this.endDate = endDate;
    }

    @JsonProperty("dev")
    public Boolean getDev(){
        return this.dev;
    }

    public void setDev(Boolean dev){
        this.dev = dev;
    }

    @JsonProperty("images")
    public Images getImages(){
        return this.images;
    }

    public void setImages(Images images){
        this.images = images;
    }

    @JsonProperty("pages")
    public Pages getPages(){
        return this.pages;
    }

    public void setPages(Pages pages){
        this.pages = pages;
    }




    public String toString(){
        StringBuilder builder = new StringBuilder();
        builder.append("Lms[").append("product : ").append(product).append(",\n")
               .append("descriptor : ").append(descriptor).append(",\n")
               .append("build : ").append(build).append(",\n")
               .append("releaseDate : ").append(releaseDate).append(",\n")
               .append("startDate : ").append(startDate).append(",\n")
               .append("endDate : ").append(endDate).append(",\n")
               .append("dev : ").append(dev).append(",\n")
               .append("images : ").append(images).append(",\n")
               .append("pages : ").append(pages).append(",\n")
               .append("]");
        return builder.toString();
    }

}
