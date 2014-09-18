package com.staples.mobile.lms.object;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.lang.StringBuilder;
import java.lang.String;

@JsonPropertyOrder({"server","path"})
public class Images {

    private String server;
    private String path;

    @JsonProperty("server")
    public String getServer(){
        return this.server;
    }

    public void setServer(String server){
        this.server = server;
    }

    @JsonProperty("path")
    public String getPath(){
        return this.path;
    }

    public void setPath(String path){
        this.path = path;
    }




    public String toString(){
        StringBuilder builder = new StringBuilder();
        builder.append("Images[").append("server : ").append(server).append(",\n")
               .append("path : ").append(path).append(",\n")
               .append("]");
        return builder.toString();
    }

}
