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

public class ProductCollection {

    private static final String TAG = "ProductCollection";

    private static final boolean LOGGING = false;

    // Instance Variables

    public interface ProductCollectionCallBack {

        public void onProductCollectionResult(ProductCollection.ProductContainer productContainer,
                                              List<ProductContainer.ERROR_CODES> errorCodes);
    }

    private ProductCollection() {
    }

    public static void getProducts(String urlExtension,
                                   String limit,
                                   String offset,
                                   Map collectionMap,
                                   ProductCollectionCallBack productCollectionCallBack) {

        if (LOGGING) Log.v(TAG, "ProductCollection:getProducts():"
                        + " limit[" + limit + "]"
                        + " offset[" + offset + "]"
                        + " urlExtension[" + urlExtension + "]"
                        + " collectionMap[" + collectionMap + "]"
                        + " productCollectionCallBack[" + productCollectionCallBack + "]"
        );

        ProductContainer productContainer = new ProductContainer();

        productContainer.urlExtension = urlExtension;
        productContainer.limit = Integer.parseInt(limit);
        productContainer.offset = Integer.parseInt(offset);
        productContainer.collectionMap = collectionMap;
        productContainer.productCollectionCallBack = productCollectionCallBack;

        productContainer.browse();
    }

    public static void test() {

        if (LOGGING) Log.v(TAG, "ProductCollection:test():");

        Map collectionMap = new HashMap<String, String>();
        // String urlExtension = "10001/category/identifier/BI739472?filterList=&limit=8&responseFormat=json";
        String urlExtension = "10001/category/identifier/BI739472?filterList=&limit=8";
        // String urlExtension = "category/identifier/BI739472";
        // String urlExtension = "category/identifier/BI739472?filterList=";
        // String urlExtension = "10001/category/identifier/CL165079?filterList=&limit=8&responseFormat=json";
        // http://sapi.staples.com/v1/10001/category/identifier/BI739472?catalogId=10051&client_id=JxP9wlnIfCSeGc9ifRAAGku7F4FSdErd&locale=en_US&offset=1&zipCode=12345&limit=100

        ProductCollection.getProducts(urlExtension,
                "50",
                "1",
                collectionMap,
                null); // ProductCollection CallBack

        /* @@@ STUBBED
        collectionMap.clear();

        identifier = "CL165079";
        collectionMap.put(ProductCollection.COLLECTION_ARGS.IDENTIFIER, identifier);
        collectionMap.put(ProductCollection.COLLECTION_ARGS.DEFAULT_LIMIT, DEFAULT_LIMIT);
        collectionMap.put(ProductCollection.COLLECTION_ARGS.OFFSET, DEFAULT_OFFSET);

        this.getProducts(collectionMap, null);
        @@@ STUBBED */
    }


    public static class ProductContainer implements Callback<Browse> {

        private static final String TAG = "ProductContainer";

        private static final String DEFAULT_FILTER_LIST = "";
        private static final int DEFAULT_LIMIT = 50;
        private static final int DEFAULT_OFFSET = 1;

        private static final String AMPERSAND = "&";
        private static final String QUESTION_MARK = "?";

        private static final String IDENTIFIER_ARG = "identifier/";
        private static final String FILTER_LIST_ARG = "filterList=";
        private static final String LIMIT_ARG = "limit=";
        private static final String OFFSET_ARG = "offset=";

        // category/identifier/CL165566
        // category/identifier/CL165566?filterList=
        // category/identifier/CL165566?filterList=&limit=8&responseFormat=json

        // 10001/category/identifier/CL165566
        // 10001/category/identifier/CL165566?filterList=
        // 10001/category/identifier/CL165566?filterList=&limit=8&responseFormat=json

        public enum ERROR_CODES {

            RAW,
            PARSE_NO_IDENTIFIER,
            PARSE_NO_URL_EXTENSION,
            RETROFIT_ERROR,
        }

        // Inputs

        private String urlExtension;
        private String identifier;
        private String filterList;
        private int limit;
        private int offset;
        private Map collectionMap;
        private ProductCollectionCallBack productCollectionCallBack;

        // Outputs

        private List<ERROR_CODES> errorCodes;

        private int recordSetCount;
        private int recordSetTotal;
        private List<Product> products;
        private RetrofitError retrofitError;

        public ProductContainer() {
            errorCodes = new ArrayList<ERROR_CODES>();
        }

        public String getIdentifier() {
            return identifier;
        }

        public int getRecordSetCount() {
            return recordSetCount;
        }

        public int getRecordSetTotal() {
            return recordSetTotal;
        }

        public List<Product> getProducts() {
            return products;
        }

        public RetrofitError getRetrofitError() {
            return retrofitError;
        }

        public void browse() {

            if (LOGGING) Log.v(TAG, "ProductContainer:browse():"
                            + " offset[" + offset + "]"
                            + " limit[" + limit + "]"
                            + " urlExtension[" + urlExtension + "]"
                            + " this[" + this + "]"
            );

            while (true) {

                if (urlExtension == null) {

                    errorCodes.add(ERROR_CODES.PARSE_NO_URL_EXTENSION);

                    handleParseError();

                    break; // while (true)
                }

                parseUrl(urlExtension);

                if (!errorCodes.isEmpty()) {

                    handleParseError();

                    break; // while (true)
                }

                Access access = Access.getInstance();

                EasyOpenApi easyOpenApi = access.getEasyOpenApi(false); // not secure

                easyOpenApi.getCategory(identifier,
                                        offset,
                                        limit,
                                        filterList,
                                        this); // callback
                break; // while (true)

            } // while (true)
        }

        private void handleParseError() {

            if (LOGGING) Log.e(TAG, "ProductContainer:handleParseError():"
                            + " this[" + this + "]"
            );

            if (productCollectionCallBack != null) {

                recordSetCount = 0;
                recordSetTotal = 0;
                products = null;

                productCollectionCallBack.onProductCollectionResult(this, errorCodes);
            }
        }

        @Override
        public void success(Browse browse, Response response) {

            if (LOGGING) Log.v(TAG, "ProductContainer:success(): Entry."
                            + " browse[" + browse + "]"
                            + " response[" + response + "]"
                            + " this[" + this + "]"
            );

            retrofitError = null;

            recordSetCount = browse.getRecordSetCount();
            recordSetTotal = browse.getRecordSetTotal();

            products = processBrowse(browse);

            if (productCollectionCallBack != null) {

                productCollectionCallBack.onProductCollectionResult(this, null);
            }

            if (LOGGING) Log.v(TAG, "ProductContainer:success(): Exit."
                            + " identifier[" + identifier + "]"
                            + " recordSetCount[" + recordSetCount + "]"
                            + " recordSetTotal[" + recordSetTotal + "]"
                            + " products[" + products + "]"
                            + " this[" + this + "]"
            );
        }

        @Override
        public void failure(RetrofitError retrofitError) {

            String retrofitErrorMsg = ApiError.getErrorMessage(retrofitError);

            if (LOGGING) Log.e(TAG, "ProductContainer:failure():"
                            + " identifier[" + identifier + "]"
                            + " retrofitErrorMsg[" + retrofitErrorMsg + "]"
                            + " retrofitError[" + retrofitError + "]"
                            + " this[" + this + "]"
            );

            if (productCollectionCallBack != null) {

                recordSetCount = 0;
                recordSetTotal = 0;
                products = null;
                this.retrofitError = retrofitError;

                errorCodes.add(ERROR_CODES.RETROFIT_ERROR);

                productCollectionCallBack.onProductCollectionResult(this, errorCodes);
            }
        }

        private List<Product> processBrowse(Browse browse) {

            if (LOGGING) Log.v(TAG, "ProductContainer:processBrowse():"
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

            if (LOGGING) Log.v(TAG, "ProductContainer:getProductsBundle():"
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

            if (LOGGING) Log.v(TAG, "ProductContainer:getProductsClass():"
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

        private void parseUrl(String urlExtension) {

            // http://sapi.staples.com/v1/10001/category/identifier/BI739472?catalogId=10051&client_id=JxP9wlnIfCSeGc9ifRAAGku7F4FSdErd&sort=ratingAsc&locale=en_US&offset=1&zipCode=12345&limit=100

            // category/identifier/CL165566
            // category/identifier/CL165566?filterList=
            // category/identifier/CL165566?filterList=&limit=8&responseFormat=json

            // 10001/category/identifier/CL165566
            // 10001/category/identifier/CL165566?filterList=
            // 10001/category/identifier/CL165566?filterList=&limit=8&responseFormat=json

            if (LOGGING) Log.v(TAG, "ProductContainer:parseUrl():"
                            + " urlExtension[" + urlExtension + "]"
                            + " this[" + this + "]"
            );

            this.identifier = extractIdentifier(urlExtension);
            this.filterList = extractFilterList(urlExtension);
            this.limit = extractLimit(urlExtension);
            this.offset = extractOffset(urlExtension);
        }

        private String extractIdentifier(String urlExtension) {

            if (LOGGING) Log.v(TAG, "ProductContainer:extractIdentifier():"
                            + " urlExtension[" + urlExtension + "]"
                            + " this[" + this + "]"
            );

            String identifier = null;

            while (true) {

                int argumentIndex = urlExtension.indexOf(IDENTIFIER_ARG);

                if (argumentIndex < 0) {
                    errorCodes.add(ERROR_CODES.PARSE_NO_IDENTIFIER);
                    break; // while (true)
                }

                int valueIndex = argumentIndex + IDENTIFIER_ARG.length();
                int terminatorIndex = urlExtension.indexOf(QUESTION_MARK, valueIndex);
                if (terminatorIndex < 0) terminatorIndex = urlExtension.length();
                identifier = urlExtension.substring(valueIndex, terminatorIndex);

                break; // while (true)

            } // while (true)

            return (identifier);
        }

        private String extractFilterList(String urlExtension) {

            if (LOGGING) Log.v(TAG, "ProductContainer:extractFilterList():"
                            + " urlExtension[" + urlExtension + "]"
                            + " this[" + this + "]"
            );

            String filterList = DEFAULT_FILTER_LIST;

            while (true) {

                int argumentIndex = urlExtension.indexOf(FILTER_LIST_ARG);

                if (argumentIndex < 0) {
                    break; // while (true)
                }

                int valueIndex = argumentIndex + FILTER_LIST_ARG.length();
                int terminatorIndex = urlExtension.indexOf(AMPERSAND, valueIndex);
                if (terminatorIndex < 0) terminatorIndex = urlExtension.length();
                filterList = urlExtension.substring(valueIndex, terminatorIndex);

                break; // while (true)

            } // while (true)

            return (filterList);
        }

        private int extractLimit(String urlExtension) {

            if (LOGGING) Log.v(TAG, "ProductContainer:extractLimit():"
                            + " urlExtension[" + urlExtension + "]"
                            + " this[" + this + "]"
            );

            String limitString = "";

            int limit = DEFAULT_LIMIT;

            while (true) {

                int argumentIndex = urlExtension.indexOf(LIMIT_ARG);

                if (argumentIndex < 0) {
                    break; // while (true)
                }

                int valueIndex = argumentIndex + LIMIT_ARG.length();
                int terminatorIndex = urlExtension.indexOf(AMPERSAND, valueIndex);
                if (terminatorIndex < 0) terminatorIndex = urlExtension.length();
                limitString = urlExtension.substring(valueIndex, terminatorIndex);
                limit = Integer.parseInt(limitString);

                break; // while (true)

            } // while (true)

            return (limit);
        }

        private int extractOffset(String urlExtension) {

            if (LOGGING) Log.v(TAG, "ProductContainer:extractOffset():"
                            + " urlExtension[" + urlExtension + "]"
                            + " this[" + this + "]"
            );

            String offsetString = "";

            int offset = DEFAULT_OFFSET;

            while (true) {

                int argumentIndex = urlExtension.indexOf(OFFSET_ARG);

                if (argumentIndex < 0) {
                    break; // while (true)
                }

                int valueIndex = argumentIndex + OFFSET_ARG.length();
                int terminatorIndex = urlExtension.indexOf(AMPERSAND, valueIndex);
                if (terminatorIndex < 0) terminatorIndex = urlExtension.length();
                offsetString = urlExtension.substring(valueIndex, terminatorIndex);
                offset = Integer.parseInt(offsetString);

                break; // while (true)

            } // while (true)

            return (offset);
        }

    } // ProductContainer
}
