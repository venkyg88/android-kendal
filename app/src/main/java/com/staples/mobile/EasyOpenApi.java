package com.staples.mobile;

import com.staples.mobile.browse.object.Browse;

import retrofit.Callback;
import retrofit.http.EncodedPath;
import retrofit.http.GET;
import retrofit.http.Headers;
import retrofit.http.Query;

/**
 * Created by pyhre001 on 9/10/14.
 */
public interface EasyOpenApi {
    @GET("/{version}/{storeId}/category/top")
    void topCategories(
        @EncodedPath("version") String version,
        @EncodedPath("storeId") String storeId,
        @Query("catalogId") String catalogId,
        @Query("locale") String locale,
        @Query("parentIdentifier") String parentIdentifier,
        @Query("zipCode") String zipCode,
        @Query("client_id") String client_id,
        Callback<Browse> callback
    );

    @GET("/{version}/{storeId}/category/identifier/{identifier}")
    void browseCategories(
        @EncodedPath("version") String version,
        @EncodedPath("storeId") String storeId,
        @EncodedPath("identifier") String path,
        @Query("catalogId") String catalogId,
        @Query("locale") String locale,
        @Query("zipCode") String zipCode,
        @Query("client_id") String client_id,
        Callback<Browse> callback
    );
}
