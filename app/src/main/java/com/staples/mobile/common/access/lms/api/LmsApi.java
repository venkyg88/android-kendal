package com.staples.mobile.common.access.lms.api;

import com.staples.mobile.common.access.lms.model.Lms;

import retrofit.Callback;
import retrofit.http.EncodedPath;
import retrofit.http.GET;

public interface LmsApi {
//    public static final String SERVICE_ENDPOINT = "http://10.4.128.46:3000"; // node.js
    public static final String SERVICE_ENDPOINT = "http://66.30.112.203:25624"; // rpi
//    public static final String SERVICE_ENDPOINT = "http://10.29.172.60:9100"; // The office printer!

    @GET("/{version}/{storeId}/lms")
    void lms(
        @EncodedPath("version") String version,
        @EncodedPath("storeId") String storeId,
        Callback<Lms> callback
    );
}
