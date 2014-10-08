package com.staples.mobile.common.access.easyopen.api;

import com.staples.mobile.common.access.easyopen.model.browse.Browse;
import com.staples.mobile.common.access.easyopen.model.sku.Sku;
import com.staples.mobile.common.access.easyopen.model.member.MemberDetail;
import com.staples.mobile.common.access.easyopen.model.login.RegisteredUserLogin;
import com.staples.mobile.common.access.easyopen.model.login.TokenObject;

import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.EncodedPath;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Query;

public interface EasyOpenApi {


    public static final String INSECURE_ENDPOINT = "http://sapi.staples.com";
//    public static final String INSECURE_ENDPOINT = "http://qapi.staples.com";
//    public static final String INSECURE_ENDPOINT = "http://10.29.172.60:9100"; // The office printer!

    public static final String SECURE_ENDPOINT = "https://sapi.staples.com";

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

    //https://sapi.staples.com/v1/10001/loginidentity?client_id=N6CA89Ti14E6PAbGTr5xsCJ2IGaHzGwS
    @POST("/{version}/{storeId}/loginidentity")
    public void registeredUserLogin(
            @Body RegisteredUserLogin body,
            @EncodedPath("version") String version,
            @EncodedPath("storeId") String storeId,
            @Query("client_id") String client_id,
            Callback<TokenObject> callback
    );

//  /v1/{storeId}/member/profile
    @GET("/{version}/{storeId}/member/profile")
    void member(
            @EncodedPath("version") String version,
            @EncodedPath("storeId") String storeId,
            @Query("locale") String locale,
            @Query("client_id") String client_id,
            Callback<MemberDetail> callback
    );

    //https://api.staples.com/v1/10001/guestidentity?client_id=N6CA89Ti14E6PAbGTr5xsCJ2IGaHzGwS
    @POST("/{version}/{storeId}/guestidentity")
    public void guestLogin(
            @EncodedPath("version") String version,
            @EncodedPath("storeId") String storeId,
            @Query("client_id") String client_id,
            Callback<TokenObject> callback
    );
}
