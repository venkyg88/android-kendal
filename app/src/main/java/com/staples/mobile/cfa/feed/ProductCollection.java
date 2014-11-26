package com.staples.mobile.cfa.feed;

import android.app.Activity;
import android.util.Log;

import com.staples.mobile.cfa.login.LoginHelper;

import com.staples.mobile.common.access.Access;
import com.staples.mobile.common.access.easyopen.api.EasyOpenApi;
import com.staples.mobile.common.access.easyopen.model.ApiError;
import com.staples.mobile.common.access.easyopen.model.browse.Browse;
import com.staples.mobile.common.access.easyopen.model.browse.Category;
import com.staples.mobile.common.access.easyopen.model.browse.Product;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class ProductCollection
        implements Callback<Browse> {

    private static final String TAG = "ProductCollection";

    private static final boolean LOGGING = false;

    // Default Values

    private static final String DEFAULT_CATALOG_ID = "10051";
    private static final String DEFAULT_CLIENT_ID = LoginHelper.CLIENT_ID;
    private static final String DEFAULT_LOCALE = "en_US";
    private static final String DEFAULT_MAX_ITEMS = "50";
    private static final String DEFAULT_OFFSET = "1";
    private static final String DEFAULT_RECOMMENDATION = "v1";
    private static final String DEFAULT_STORE_ID = "10001";
    private static final String DEFAULT_ZIP_CODE = "01010";

    private Access access;
    private EasyOpenApi easyOpenApi;

    public enum COLLECTION_ARGS {

        RAW,
        CATALOG_ID,
        CLIENT_ID,
        IDENTIFIER,
        LOCALE,
        MAX_ITEMS,
        OFFSET,
        RECOMMENDATION,
        STORE_ID,
        ZIP_CODE,
    }

    // Instance Variables

    private String identifier;

    private ProductCollectionCallBack productCollectionCallBack;

    private ProductContainer productContainer;

    private Map collectionArgs;

    public interface ProductCollectionCallBack {

        public void onProductCollectionResult(ProductCollection.ProductContainer productContainer);
    }

    public static class ProductContainer {

        public int recordSetCount;

        public int recordSetTotal;

        public List<Product> products;
    }

    public ProductCollection() {

        if (LOGGING) Log.v(TAG, "ProductCollection:ProductCollection():"
                        + " this[" + this + "]"
        );

        access = Access.getInstance();

        easyOpenApi = access.getEasyOpenApi(false); // not secure
    }

    public void getProducts(Map collectionArgs,
                            ProductCollectionCallBack productCollectionCallBack) {

        if (LOGGING) Log.v(TAG, "ProductCollection:getProducts():"
                        + " collectionArgs[" + collectionArgs + "]"
                        + " productCollectionCallBack[" + productCollectionCallBack + "]"
                        + " this[" + this + "]"
        );

        this.collectionArgs = collectionArgs;
        this.productCollectionCallBack = productCollectionCallBack;

        this.identifier = (String) collectionArgs.get(COLLECTION_ARGS.IDENTIFIER);

        String catalogId = (String) collectionArgs.get(COLLECTION_ARGS.CATALOG_ID);
        if (catalogId == null) catalogId = DEFAULT_CATALOG_ID;

        String clientId = (String) collectionArgs.get(COLLECTION_ARGS.CLIENT_ID);
        if (clientId == null) clientId = DEFAULT_CLIENT_ID;

        String locale = (String) collectionArgs.get(COLLECTION_ARGS.LOCALE);
        if (locale == null) locale = DEFAULT_LOCALE;

        String maxItems = (String) collectionArgs.get(COLLECTION_ARGS.MAX_ITEMS);
        if (maxItems == null) maxItems = DEFAULT_MAX_ITEMS;
        int maxItemsInt = Integer.parseInt(maxItems);

        String offset = (String) collectionArgs.get(COLLECTION_ARGS.OFFSET);
        if (offset == null) offset = DEFAULT_OFFSET;
        int offsetInt = Integer.parseInt(offset);

        String recommendation = (String) collectionArgs.get(COLLECTION_ARGS.RECOMMENDATION);
        if (recommendation == null) recommendation = DEFAULT_RECOMMENDATION;

        String storeId = (String) collectionArgs.get(COLLECTION_ARGS.STORE_ID);
        if (storeId == null) storeId = DEFAULT_STORE_ID;

        String zipCode = (String) collectionArgs.get(COLLECTION_ARGS.ZIP_CODE);
        if (zipCode == null) zipCode = DEFAULT_ZIP_CODE;

        easyOpenApi.browseCategories(recommendation,
                storeId,
                identifier,
                catalogId,
                locale,
                zipCode,
                clientId,
                offsetInt,
                maxItemsInt,
                this); // callback
    }

    public void test() {

        if (LOGGING) Log.v(TAG, "ProductCollection:test():"
                        + " this[" + this + "]"
        );

        Map collectionArgs = new HashMap<String, String>();

        String identifier = null;

        /* @@@ STUBBED
        identifier = "BI739472";
        collectionArgs.put(ProductCollection.COLLECTION_ARGS.IDENTIFIER, identifier);
        collectionArgs.put(ProductCollection.COLLECTION_ARGS.MAX_ITEMS, DEFAULT_MAX_ITEMS);
        collectionArgs.put(ProductCollection.COLLECTION_ARGS.OFFSET, DEFAULT_OFFSET);

        this.getProducts(collectionArgs, null);
        @@@ STUBBED */

        /* @@@ STUBBED
        collectionArgs.clear();

        identifier = "CL165079";
        collectionArgs.put(ProductCollection.COLLECTION_ARGS.IDENTIFIER, identifier);
        collectionArgs.put(ProductCollection.COLLECTION_ARGS.MAX_ITEMS, DEFAULT_MAX_ITEMS);
        collectionArgs.put(ProductCollection.COLLECTION_ARGS.OFFSET, DEFAULT_OFFSET);

        this.getProducts(collectionArgs, null);
        @@@ STUBBED */
    }

    @Override
    public void success(Browse browse, Response response) {

        if (LOGGING) Log.v(TAG, "ProductCollection:success(): Entry."
                        + " browse[" + browse + "]"
                        + " response[" + response + "]"
                        + " this[" + this + "]"
        );

        productContainer = new ProductContainer();
        productContainer.recordSetCount = browse.getRecordSetCount();
        productContainer.recordSetTotal = browse.getRecordSetTotal();
        productContainer.products = processBrowse(browse);

        if (productCollectionCallBack != null) productCollectionCallBack.onProductCollectionResult(productContainer);

        if (LOGGING) Log.v(TAG, "ProductCollection:success(): Exit."
                        + " productContainer.recordSetCount[" + productContainer.recordSetCount + "]"
                        + " productContainer.recordSetTotal[" + productContainer.recordSetTotal + "]"
                        + " productContainer.products[" + productContainer.products + "]"
                        + " this[" + this + "]"
        );
    }

    @Override
    public void failure(RetrofitError retrofitError) {

        String retrofitErrorMsg = ApiError.getErrorMessage(retrofitError);

        if (LOGGING) Log.e(TAG, "ProductCollection:failure():"
                        + " retrofitErrorMsg[" + retrofitErrorMsg + "]"
                        + " retrofitError[" + retrofitError + "]"
                        + " this[" + this + "]"
        );

        if (productCollectionCallBack != null) {

            productContainer = new ProductContainer();
            productContainer.recordSetCount = 0;
            productContainer.recordSetTotal = 0;
            productContainer.products = null;

            productCollectionCallBack.onProductCollectionResult(productContainer);
        }
    }

    private List<Product> processBrowse(Browse browse) {

        if (LOGGING) Log.v(TAG, "ProductCollection:processBrowse():"
                        + " browse[" + browse + "]"
                        + " this[" + this + "]"
        );

        List<Product> products = null;

        while (true) {

            if (browse == null) break; // while (true)

            if (identifier.startsWith("B")) {
                products = getProductsBundle(browse);
                break; // while (true)
            }

            if (identifier.startsWith("C")) {
                products = getProductsClass(browse);
                break; // while (true)
            }

            break; // while (true)

        } // while (true)

        return (products);
    }

    private List<Product> getProductsBundle(Browse browse) {

        if (LOGGING) Log.v(TAG, "ProductCollection:getProductsBundle():"
                        + " browse[" + browse + "]"
                        + " this[" + this + "]"
        );

        List<Product> products = new ArrayList<Product>();
        List<Product> promoProducts = null;

        while (true) {

            List<Category> categories = browse.getCategory();
            if (categories == null) break; // while (true)
            if (categories.size() <= 0) break; // while (true)

            Category category = categories.get(0);
            if (category == null) break; // while (true)

            List<Category> promoCategories = category.getPromoCategory();
            if (promoCategories == null) break; // while (true)

            for (Category promoCategory : promoCategories) {

                promoProducts = promoCategory.getProduct();
                products.addAll(promoProducts);
            }

            break; // while (true)

        } // while (true)

        return (products);
    }

    private List<Product> getProductsClass(Browse browse) {

        if (LOGGING) Log.v(TAG, "ProductCollection:getProductsClass():"
                        + " browse[" + browse + "]"
                        + " this[" + this + "]"
        );

        List<Product> products = null;

        while (true) {

            List<Category> categories = browse.getCategory();
            if (categories == null) break; // while (true)
            if (categories.size() <= 0) break; // while (true)

            Category category = categories.get(0);
            if (category == null) break; // while (true)

            products = category.getProduct();

            break; // while (true)

        } // while (true)

        return (products);
    }
}