package com.staples.mobile.common.access.easyopen.api;

import com.staples.mobile.common.access.easyopen.model.browse.Browse;
import com.staples.mobile.common.access.easyopen.model.cart.CartContents;
import com.staples.mobile.common.access.easyopen.model.cart.CartUpdate;
import com.staples.mobile.common.access.easyopen.model.cart.DeleteFromCart;
import com.staples.mobile.common.access.easyopen.model.cart.TypedJsonString;
import com.staples.mobile.common.access.easyopen.model.login.RegisteredUserLogin;
import com.staples.mobile.common.access.easyopen.model.login.TokenObject;
import com.staples.mobile.common.access.easyopen.model.member.MemberDetail;
import com.staples.mobile.common.access.easyopen.model.sku.Sku;

import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.DELETE;
import retrofit.http.EncodedPath;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Query;

public interface EasyOpenApi {
    public static final String INSECURE_ENDPOINT = "http://sapi.staples.com";
//    public static final String INSECURE_ENDPOINT = "http://qapi.staples.com";
//    public static final String INSECURE_ENDPOINT = "http://10.29.172.60:9100"; // The office printer!

    public static final String SECURE_ENDPOINT = "https://sapi.staples.com";

    // Browsing & product details

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
    void getSkuInfo(
        @EncodedPath("version") String version,
        @EncodedPath("storeId") String storeId,
        @EncodedPath("productId") String productId,
        @Query("catalogId") String catalogId,
        @Query("locale") String locale,
        @Query("zipCode") String zipCode,
        @Query("client_id") String client_id,
        @Query("offset") Integer offset,
        @Query("limit") Integer limit,
        Callback<Sku> callback
    );

    // Logins & profile

    @POST("/{version}/{storeId}/guestidentity")
    public void guestLogin(
        @EncodedPath("version") String version,
        @EncodedPath("storeId") String storeId,
        @Query("client_id") String client_id,
        Callback<TokenObject> callback
    );

    @POST("/{version}/{storeId}/loginidentity")
    public void registeredUserLogin(
        @Body RegisteredUserLogin body,
        @EncodedPath("version") String version,
        @EncodedPath("storeId") String storeId,
        @Query("client_id") String client_id,
        Callback<TokenObject> callback
    );

    @GET("/{version}/{storeId}/member/profile")
    void member(
        @EncodedPath("version") String version,
        @EncodedPath("storeId") String storeId,
        @Query("locale") String locale,
        @Query("client_id") String client_id,
        Callback<MemberDetail> callback
    );

    // http://api.staples.com/v1/10001/cart?locale=en_US&zipCode=05251&catalogId=10051&client_id={client-id}
    @GET("/{version}/{storeId}/cart")
    void viewCart(
            @EncodedPath("version") String version,
            @EncodedPath("storeId") String storeId,
            @Query("locale") String locale,
            @Query("zipCode") String zipCode,
            @Query("catalogId") String catalogId,
            @Query("client_id") String client_id,
            Callback<CartContents> callback
    );

    //http://api.staples.com/v1/10001/cart?locale=en_US&zipCode=05251&catalogId=10051&client_id={client-id}
    @POST("/{version}/{storeId}/cart")
    void addToCart(
            @Body TypedJsonString body,
            @EncodedPath("version") String version,
            @EncodedPath("storeId") String storeId,
            @Query("locale") String locale,
            @Query("zipCode") String zipCode,
            @Query("catalogId") String catalogId,
            @Query("client_id") String client_id,
            Callback<CartUpdate> callback
    );

    //https://api.staples.com/v1/10001/cart?locale=en_US&zipCode=05251&catalogId=10051&client_id={client-id}
    @POST("/{version}/{storeId}/cart")
    void updateCart(
            @Body TypedJsonString body,
            @EncodedPath("version") String version,
            @EncodedPath("storeId") String storeId,
            @Query("locale") String locale,
            @Query("zipCode") String zipCode,
            @Query("catalogId") String catalogId,
            @Query("client_id") String client_id,
            Callback<CartUpdate> callback
    );

    //http://api.staples.com/v1/10001/cart/id/453387856?locale=en_US&client_id={client-id}
    @DELETE("/{version}/{storeId}/cart/id/{orderItemId}")
    void deleteFromCart(
            @EncodedPath("version") String version,
            @EncodedPath("storeId") String storeId,
            @EncodedPath("orderItemId") String orderItemId,
            @Query("locale") String locale,
            @Query("client_id") String client_id,
            Callback<DeleteFromCart> callback
    );
}
