package com.staples.mobile.lms.object;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.lang.StringBuilder;

@JsonPropertyOrder({"landing"})
public class Pages {

    private Landing landing;

    @JsonProperty("landing")
    public Landing getLanding(){
        return this.landing;
    }

    public void setLanding(Landing landing){
        this.landing = landing;
    }




    public String toString(){
        StringBuilder builder = new StringBuilder();
        builder.append("Pages[").append("landing : ").append(landing).append(",\n")
               .append("]");
        return builder.toString();
    }

}
