package com.staples.mobile.lms.object;

import java.util.ArrayList;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.lang.StringBuilder;
import java.lang.String;

@JsonPropertyOrder({"sku","title","bullet","image","price"})
public class ExplodedSku {

    private String sku;
    private String title;
    private List<String> bullet = new ArrayList<String> ();
    private String image;
    private Price price;

    @JsonProperty("sku")
    public String getSku(){
        return this.sku;
    }

    public void setSku(String sku){
        this.sku = sku;
    }

    @JsonProperty("title")
    public String getTitle(){
        return this.title;
    }

    public void setTitle(String title){
        this.title = title;
    }

    @JsonProperty("bullet")
    public List<String> getBullet(){
        return this.bullet;
    }

    public void setBullet(List<String> bullet){
        this.bullet = bullet;
    }

    @JsonProperty("image")
    public String getImage(){
        return this.image;
    }

    public void setImage(String image){
        this.image = image;
    }

    @JsonProperty("price")
    public Price getPrice(){
        return this.price;
    }

    public void setPrice(Price price){
        this.price = price;
    }




    public String toString(){
        StringBuilder builder = new StringBuilder();
        builder.append("ExplodedSku[").append("sku : ").append(sku).append(",\n")
               .append("title : ").append(title).append(",\n")
               .append("bullet : ").append(bullet).append(",\n")
               .append("image : ").append(image).append(",\n")
               .append("price : ").append(price).append(",\n")
               .append("]");
        return builder.toString();
    }

}
