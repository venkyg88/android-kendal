package com.staples.mobile.lms;

public class Image {
    private String server;
    private String path;

    public String getServer() {
        return server;
    }

    public String getPath() {
        return path;
    }

    public String toString(){
        StringBuilder builder = new StringBuilder();
        builder.append("Image[").append("server : ").append(server).append(",\n")
                .append("path : ").append(path).append(",\n")
                .append("]");
        return builder.toString();
    }
}
