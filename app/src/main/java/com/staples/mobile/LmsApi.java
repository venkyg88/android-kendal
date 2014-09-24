package com.staples.mobile;

import com.staples.mobile.lms.object.Lms;

import retrofit.Callback;
import retrofit.http.EncodedPath;
import retrofit.http.GET;

public interface LmsApi {
    public static final String SERVICE_ENDPOINT = "http://10.4.128.46:3000";

    @GET("/{version}/{storeId}/lms")
    void lms(
        @EncodedPath("version") String version,
        @EncodedPath("storeId") String storeId,
        Callback<Lms> callback
    );
}
