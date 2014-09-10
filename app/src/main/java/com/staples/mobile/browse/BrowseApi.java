package com.staples.mobile.browse;

import com.staples.mobile.object.Browse;

import retrofit.Callback;
import retrofit.http.EncodedPath;
import retrofit.http.GET;
import retrofit.http.Query;

/**
 * Created by pyhre001 on 9/10/14.
 */
public interface BrowseApi {
    @GET("/{version}/{storeId}/{path}")
    void browse(
            @EncodedPath("version") String version,
            @EncodedPath("storeId") String storeId,
            @EncodedPath("path") String path,
            @Query("catalogId") String catalogId,
            @Query("locale") String locale,
            @Query("zipCode") String zipCode,
            @Query("client_id") String client_id,
            Callback<Browse> callback
    );
}
