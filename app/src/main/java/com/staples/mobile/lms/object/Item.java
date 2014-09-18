package com.staples.mobile.lms.object;

import java.util.ArrayList;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.lang.StringBuilder;
import java.lang.String;

@JsonPropertyOrder({"banner","title","promoText","display","compactSku","explodedSku"})
public class Item {

    private String banner;
    private String title;
    private String promoText;
    private String display;
    private List<String> compactSku = new ArrayList<String> ();
    private List<ExplodedSku> explodedSku = new ArrayList<ExplodedSku> ();

    @JsonProperty("banner")
    public String getBanner(){
        return this.banner;
    }

    public void setBanner(String banner){
        this.banner = banner;
    }

    @JsonProperty("title")
    public String getTitle(){
        return this.title;
    }

    public void setTitle(String title){
        this.title = title;
    }

    @JsonProperty("promo_text")
    public String getPromoText(){
        return this.promoText;
    }

    public void setPromoText(String promoText){
        this.promoText = promoText;
    }

    @JsonProperty("display")
    public String getDisplay(){
        return this.display;
    }

    public void setDisplay(String display){
        this.display = display;
    }

    @JsonProperty("compact_sku")
    public List<String> getCompactSku(){
        return this.compactSku;
    }

    public void setCompactSku(List<String> compactSku){
        this.compactSku = compactSku;
    }

    @JsonProperty("exploded_sku")
    public List<ExplodedSku> getExplodedSku(){
        return this.explodedSku;
    }

    public void setExplodedSku(List<ExplodedSku> explodedSku){
        this.explodedSku = explodedSku;
    }




    public String toString(){
        StringBuilder builder = new StringBuilder();
        builder.append("Item[").append("banner : ").append(banner).append(",\n")
               .append("title : ").append(title).append(",\n")
               .append("promoText : ").append(promoText).append(",\n")
               .append("display : ").append(display).append(",\n")
               .append("compactSku : ").append(compactSku).append(",\n")
               .append("explodedSku : ").append(explodedSku).append(",\n")
               .append("]");
        return builder.toString();
    }

}
