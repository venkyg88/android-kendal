package com.staples.mobile.lms.object;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.ArrayList;
import java.util.List;

@JsonPropertyOrder({"product","descriptor","build","releaseDate","startDate","endDate","dev","style","image","page"})
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

    @JsonProperty("style")
    public Style getStyle(){
        return this.style;
    }

    public void setStyle(Style style){
        this.style = style;
    }

    @JsonProperty("image")
    public Image getImage(){
        return this.image;
    }

    public void setImage(Image image){
        this.image = image;
    }

    @JsonProperty("page")
    public List<Page> getPage(){
        return this.page;
    }

    public void setPage(List<Page> page){
        this.page = page;
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
