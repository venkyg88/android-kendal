package com.staples.mobile.common.access.easyopen.api;

import com.staples.mobile.common.access.easyopen.model.browse.Browse;
import com.staples.mobile.common.access.easyopen.model.cart.AddressDetail;
import com.staples.mobile.common.access.easyopen.model.cart.CartContents;
import com.staples.mobile.common.access.easyopen.model.cart.CartUpdate;
import com.staples.mobile.common.access.easyopen.model.cart.DeleteFromCart;
import com.staples.mobile.common.access.easyopen.model.cart.TypedJsonString;
import com.staples.mobile.common.access.easyopen.model.login.CreateUserLogin;
import com.staples.mobile.common.access.easyopen.model.inventory.StoreInfo;
import com.staples.mobile.common.access.easyopen.model.inventory.StoreInventory;
import com.staples.mobile.common.access.easyopen.model.login.RegisteredUserLogin;
import com.staples.mobile.common.access.easyopen.model.login.TokenObject;
import com.staples.mobile.common.access.easyopen.model.member.MemberDetail;
import com.staples.mobile.common.access.easyopen.model.sku.SkuDetails;
import com.staples.mobile.common.access.easyopen.model.browse.SearchResult;

import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.DELETE;
import retrofit.http.EncodedPath;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Query;

public interface EasyOpenApi {
    public static final String INSECURE_ENDPOINT = "http://sapi.staples.com";
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

    @GET("/{version}/{storeId}/product/partnumber/{productId}/details")
    void getSkuDetails(
        @EncodedPath("version") String version,
        @EncodedPath("storeId") String storeId,
        @EncodedPath("productId") String productId,
        @Query("catalogId") String catalogId,
        @Query("locale") String locale,
        @Query("zipCode") String zipCode,
        @Query("client_id") String client_id,
        @Query("offset") Integer offset,
        @Query("limit") Integer limit,
        Callback<SkuDetails> callback
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

    // https://api.staples.com/v1/10001/member/registeruser?locale=en_US&client_id={your-client-id}
    @POST("/{version}/{storeId}/member/registeruser")
    public void registerUser(
            @Body CreateUserLogin body,
            @EncodedPath("version") String version,
            @EncodedPath("storeId") String storeId,
            @Query("locale") String locale,
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
            @Query("offset") Integer offset,
            @Query("limit") Integer limit,
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

    //http://sapi.staples.com/v1/10001/stores/inventory?locale=en_US&zipCode=05251&catalogId=10051&partNumber=513096&distance=100&client_id=N6CA89Ti14E6PAbGTr5xsCJ2IGaHzGwS
    @GET("/{version}/{storeId}/stores/inventory")
    void getStoreInventory(
            @EncodedPath("version") String version,
            @EncodedPath("storeId") String storeId,
            @Query("locale") String locale,
            @Query("zipCode") String zipCode,
            @Query("catalogId") String catalogId,
            @Query("partNumber") String partNumber,
            @Query("distance") String distance,
            @Query("client_id") String client_id,
            @Query("offset") Integer offset,
            @Query("limit") Integer limit,
            Callback<StoreInventory> callback
    );

    //http://sapi.staples.com/v1/10001/stores/info/1113?locale=en_US&client_id=N6CA89Ti14E6PAbGTr5xsCJ2IGaHzGwS
    @GET("/{version}/{storeId}/stores/info/{storeNumber}")
    void getStoreDetails(
            @EncodedPath("version") String version,
            @EncodedPath("storeId") String storeId,
            @EncodedPath("storeNumber") String storeNumber,
            @Query("locale") String locale,
            @Query("client_id") String client_id,
            Callback<StoreInfo> callback
    );

    //http://sapi.staples.com/v1/10001/search/term?catalogId=10051&locale=en_US&zipCode=01702&term=laptops&page=1&limit=10&sort=0&client_id=N6CA89Ti14E6PAbGTr5xsCJ2IGaHzGwS&filterId=
    @GET("/{version}/{storeId}/search/term")
    void searchResult(
            @EncodedPath("version") String version,
            @EncodedPath("storeId") String storeId,
            @Query("catalogId") String catalogId,
            @Query("locale") String locale,
            @Query("zipCode") String zipCode,
            @Query("term") String term,
            @Query("page") Integer page,
            @Query("limit") Integer limit,
            @Query("sort") Integer sort,
            @Query("client_id") String client_id,
            @Query("filterId") String filterIds,
            Callback<SearchResult> callback
    );

    //https://api.staples.com/v1/10001/cart/address/billing?locale=en_US&client_id=
    @GET("/{version}/{storeId}/cart/address/billing")
    void getBillingAddress(
            @EncodedPath("version") String version,
            @EncodedPath("storeId") String storeId,
            @Query("locale") String locale,
            @Query("client_id") String client_id,
            Callback<AddressDetail> callback
    );

    //https://api.staples.com/v1/10001/cart/address/shipping?locale=en_US&client_id=
    @GET("/{version}/{storeId}/cart/address/shipping")
    void getShippingAddress(
            @EncodedPath("version") String version,
            @EncodedPath("storeId") String storeId,
            @Query("locale") String locale,
            @Query("client_id") String client_id,
            Callback<AddressDetail> callback
    );

}
