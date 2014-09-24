package com.staples.mobile.common.access.easyopen.api;

import com.staples.mobile.common.access.easyopen.model.sku.Sku;
import com.staples.mobile.cfa.easyopen.Browse;

import retrofit.Callback;
import retrofit.http.EncodedPath;
import retrofit.http.GET;
import retrofit.http.Query;

public interface EasyOpenApi {
    public static final String SERVICE_ENDPOINT = "http://sapi.staples.com";
//    public static final String SERVICE_ENDPOINT = "http://10.29.172.60:9100"; // The office printer!

    @GET("/{version}/{storeId}/category/top")
    void topCategories(
        @EncodedPath("version") String version,
        @EncodedPath("storeId") String storeId,
        @Query("catalogId") String catalogId,
        @Query("locale") String locale,
        @Query("parentIdentifier") String parentIdentifier,
        @Query("zipCode") String zipCode,
        @Query("client_id") String client_id,
        @Query("offset") Integer offset,
        @Query("limit") Integer limit,
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
        @Query("offset") Integer offset,
        @Query("limit") Integer limit,
        Callback<Browse> callback
    );

    //http://qapi.staples.com/v1/10001/product/partnumber/606806?catalogId=10051&locale=en_US&zipCode=01702&client_id=N6CA89Ti14E6PAbGTr5xsCJ2IGaHzGwS
    //"/{version}/{storeId}/product/partnumber/{productId}?catalogId={catalogId}&locale={locale}&zipCode={zipCode}&client_id={clientId}"
    @GET("/{version}/{storeId}/product/partnumber/{productId}")
    void sku(
            @EncodedPath("version") String version,
            @EncodedPath("storeId") String storeId,
            @EncodedPath("productId") String productId,
            @Query("catalogId") String catalogId,
            @Query("locale") String locale,
            @Query("zipCode") String zipCode,
            @Query("client_id") String client_id,
            Callback<Sku> callback
    );
}
