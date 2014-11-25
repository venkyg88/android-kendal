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
import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class DailyDealProductCollection
        implements Callback<Browse> {

    private static final String TAG = "ProductCollection";

    private static final boolean LOGGING = false;

    private static final String CATALOG_ID = "10051";
    private static final String CLIENT_ID = LoginHelper.CLIENT_ID;
    private static final String LOCALE = "en_US";
    private static final String RECOMMENDATION = "v1";
    private static final String STORE_ID = "10001";
    private static final String ZIPCODE = "01010";

    private Access access;
    private EasyOpenApi easyOpenApi;

    public interface ProductCollectionCallBack {

        public void onProductCollectionResult(List<Product> products);
    }

    // Instance Variables

    private ProductCollectionCallBack productCollectionCallBack;

    private String identifier;

    private List<Product> products;

    public DailyDealProductCollection() {

        if (LOGGING) Log.v(TAG, "ProductCollection:ProductCollection():"
                        + " this[" + this + "]"
        );

        access = Access.getInstance();

        easyOpenApi = access.getEasyOpenApi(false); // not secure
    }

    public void getProducts(String identifier,
                            int offset,
                            int maxItems,
                            ProductCollectionCallBack productCollectionCallBack) {

        if (LOGGING) Log.v(TAG, "ProductCollection:getProducts():"
                        + " identifier[" + identifier + "]"
                        + " offset[" + offset + "]"
                        + " maxItems[" + maxItems + "]"
                        + " productCollectionCallBack[" + productCollectionCallBack + "]"
                        + " this[" + this + "]"
        );

        this.identifier = identifier;
        this.productCollectionCallBack = productCollectionCallBack;

        easyOpenApi.browseCategories(RECOMMENDATION,
                STORE_ID,
                identifier,
                CATALOG_ID,
                LOCALE,
                ZIPCODE,
                CLIENT_ID,
                offset,
                maxItems,
                this); // callback
    }

    @Override
    public void success(Browse browse, Response response) {

        if (LOGGING) Log.v(TAG, "ProductCollection:success(): Entry."
                        + " browse[" + browse + "]"
                        + " response[" + response + "]"
                        + " this[" + this + "]"
        );

        products = processBrowse(browse);

        if (productCollectionCallBack != null) productCollectionCallBack.onProductCollectionResult(products);

        if (LOGGING) Log.v(TAG, "ProductCollection:success(): Exit."
                        + " products[" + products + "]"
                        + " this[" + this + "]"
        );
    }

    @Override
    public void failure(RetrofitError retrofitError) {

        products = null;

        String retrofitErrorMsg = ApiError.getErrorMessage(retrofitError);

        if (LOGGING) Log.e(TAG, "ProductCollection:failure():"
                        + " retrofitErrorMsg[" + retrofitErrorMsg + "]"
                        + " retrofitError[" + retrofitError + "]"
                        + " this[" + this + "]"
        );

        if (productCollectionCallBack != null) productCollectionCallBack.onProductCollectionResult(products);
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
