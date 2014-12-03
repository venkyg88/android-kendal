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

    // Default Values

    private static final String DEFAULT_CATALOG_ID = "10051";
    private static final String DEFAULT_CLIENT_ID = LoginHelper.CLIENT_ID;
    private static final String DEFAULT_LOCALE = "en_US";
    private static final String DEFAULT_LIMIT = "50";
    private static final String DEFAULT_OFFSET = "1";
    private static final String DEFAULT_VERSION = "v1";
    private static final String DEFAULT_STORE_ID = "10001";
    private static final String DEFAULT_ZIP_CODE = "01010";

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

    public interface ProductCollectionCallBack {

        public void onProductCollectionResult(ProductCollection.ProductContainer productContainer,
                                              List<ProductContainer.ERROR_CODES> errorCodes);
    }

    public static class ProductContainer implements Callback<Browse> {

        public enum ERROR_CODES {

            RAW,
            PARSE_NO_IDENTIFIER,
            PARSE_NO_URL_EXTENSION,
            RETROFIT_ERROR,
        }

        // Inputs

        private String identifier;
        private String urlExtension;
        private String limit;
        private String offset;
        private Map collectionMap;
        private ProductCollectionCallBack productCollectionCallBack;

        // Outputs

        public List<ERROR_CODES> errorCodes;

        public int recordSetCount;
        public int recordSetTotal;
        public List<Product> products;

        public ProductContainer() {

            errorCodes = new ArrayList<ERROR_CODES>();
        }

        public void browse() {

            if (LOGGING) Log.v(TAG, "ProductContainer:browse():"
                            + " offset[" + offset + "]"
                            + " limit[" + limit + "]"
                            + " urlExtension[" + urlExtension + "]"
                            + " this[" + this + "]"
            );

            // category/identifier/CL165566?filterList=&limit=8&responseFormat=json
            // 10001/category/identifier/CL165566?filterList=&limit=8&responseFormat=json

            while (true) {

                if (urlExtension == null) {

                    errorCodes.add(ERROR_CODES.PARSE_NO_URL_EXTENSION);

                    handleParseError();

                    break; // while (true)
                }

                urlExtension = parseUrl(urlExtension);

                if ( ! errorCodes.isEmpty()) {

                    handleParseError();

                    break; // while (true)
                }

                Access access = Access.getInstance();

                EasyOpenApi easyOpenApi = access.getEasyOpenApi(false); // not secure

                easyOpenApi.browseCategoriesUrl(urlExtension, this); // callback

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

        private String parseUrl(String urlExtension) {

            // http://sapi.staples.com/v1/10001/category/identifier/BI739472?catalogId=10051&client_id=JxP9wlnIfCSeGc9ifRAAGku7F4FSdErd&sort=ratingAsc&locale=en_US&offset=1&zipCode=12345&limit=100

            // category/identifier/CL165566?filterList=&limit=8&responseFormat=json
            // 10001/category/identifier/CL165566?filterList=&limit=8&responseFormat=json

            if (LOGGING) Log.v(TAG, "ProductContainer:parseUrl():"
                            + " urlExtension[" + urlExtension + "]"
                            + " this[" + this + "]"
            );

            this.identifier = extractIdentifier(urlExtension);

            String newUrlExtension = setOffset(urlExtension);
            newUrlExtension = setLimit(newUrlExtension);

            return (newUrlExtension);
        }

        private String extractIdentifier(String urlExtension) {

            // category/identifier/CL165566?filterList=&limit=8&responseFormat=json
            // 10001/category/identifier/CL165566?filterList=&limit=8&responseFormat=json

            if (LOGGING) Log.v(TAG, "ProductContainer:extractIdentifier():"
                            + " urlExtension[" + urlExtension + "]"
                            + " this[" + this + "]"
            );

            String identifier = null;

            while (true) {

                int argumentIndex = urlExtension.indexOf("identifier/");

                if (argumentIndex < 0) {
                    errorCodes.add(ERROR_CODES.PARSE_NO_IDENTIFIER);
                    break; // while (true)
                }

                int valueIndex = argumentIndex + "identifier/".length();
                int terminatorIndex = urlExtension.indexOf("?", valueIndex);
                if (terminatorIndex == -1) terminatorIndex = urlExtension.length();
                identifier = urlExtension.substring(valueIndex, terminatorIndex);

                break; // while (true)

            } // while (true)

            return (identifier);
        }

        private String setOffset(String urlExtension) {

            // http://sapi.staples.com/v1/10001/category/identifier/BI739472?catalogId=10051&client_id=JxP9wlnIfCSeGc9ifRAAGku7F4FSdErd&sort=ratingAsc&locale=en_US&offset=1&zipCode=12345&limit=100

            // category/identifier/CL165566?filterList=&limit=8&responseFormat=json
            // 10001/category/identifier/CL165566?filterList=&limit=8&responseFormat=json

            if (LOGGING) Log.v(TAG, "ProductContainer:setOffset():"
                            + " this.offset[" + this.offset + "]"
                            + " urlExtension[" + urlExtension + "]"
                            + " this[" + this + "]"
            );

            StringBuilder newUrlExtension = new StringBuilder(urlExtension);

            while (true) {

                int argumentIndex = newUrlExtension.indexOf("&offset=");

                if (argumentIndex < 0) {
                    // No offset in URL. Add default offset to URL.
                    newUrlExtension.append("&offset=");
                    newUrlExtension.append(DEFAULT_OFFSET);
                    break; // while (true)
                }

                if (this.offset == null) {
                    // We have an offset in the URL but no override supplied.
                    break; // while (true)
                }

                // We have an offset in the URL and an override was supplied.
                // Replace offset in URL with override.

                int valueIndex = argumentIndex + "&offset=".length();
                int terminatorIndex = newUrlExtension.indexOf("&", valueIndex);
                if (terminatorIndex == -1) terminatorIndex = newUrlExtension.length();

                newUrlExtension.replace(valueIndex, terminatorIndex, this.offset);

                break; // while (true)

            } // while (true)

            return (newUrlExtension.toString());
        }

        private String setLimit(String urlExtension) {

            // http://sapi.staples.com/v1/10001/category/identifier/BI739472?catalogId=10051&client_id=JxP9wlnIfCSeGc9ifRAAGku7F4FSdErd&sort=ratingAsc&locale=en_US&offset=1&zipCode=12345&limit=100

            // category/identifier/CL165566?filterList=&limit=8&responseFormat=json
            // 10001/category/identifier/CL165566?filterList=&limit=8&responseFormat=json

            if (LOGGING) Log.v(TAG, "ProductContainer:setLimit():"
                            + " this.limit[" + this.limit + "]"
                            + " urlExtension[" + urlExtension + "]"
                            + " this[" + this + "]"
            );

            StringBuilder newUrlExtension = new StringBuilder(urlExtension);

            while (true) {

                int argumentIndex = newUrlExtension.indexOf("&limit=");

                if (argumentIndex < 0) {
                    // No limit in URL. Add default limit to URL.
                    newUrlExtension.append("&limit=");
                    newUrlExtension.append(DEFAULT_LIMIT);
                    break; // while (true)
                }

                if (this.limit == null) {
                    // We have a limit in the URL but no override supplied.
                    break; // while (true)
                }

                // We have a limit in the URL and an override was supplied.
                // Replace limit in URL with override.

                int valueIndex = argumentIndex + "&limit=".length();
                int terminatorIndex = newUrlExtension.indexOf("&", valueIndex);
                if (terminatorIndex == -1) terminatorIndex = newUrlExtension.length();

                newUrlExtension.replace(valueIndex, terminatorIndex, this.limit);

                break; // while (true)

            } // while (true)

            return (newUrlExtension.toString());
        }

    } // ProductContainer

    private ProductCollection() {
    }

    static public void getProducts(String urlExtension,
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
        productContainer.limit = limit;
        productContainer.offset = offset;
        productContainer.collectionMap = collectionMap;
        productContainer.productCollectionCallBack = productCollectionCallBack;

        productContainer.browse();
    }

    public void test() {

        if (LOGGING) Log.v(TAG, "ProductCollection:test():"
                        + " this[" + this + "]"
        );

        Map collectionMap = new HashMap<String, String>();

        String identifier = null;

        /* @@@ STUBBED
        identifier = "BI739472";
        collectionMap.put(ProductCollection.COLLECTION_ARGS.IDENTIFIER, identifier);
        collectionMap.put(ProductCollection.COLLECTION_ARGS.MAX_ITEMS, DEFAULT_MAX_ITEMS);
        collectionMap.put(ProductCollection.COLLECTION_ARGS.OFFSET, DEFAULT_OFFSET);

        this.getProducts(collectionMap, null);
        @@@ STUBBED */

        /* @@@ STUBBED
        collectionMap.clear();

        identifier = "CL165079";
        collectionMap.put(ProductCollection.COLLECTION_ARGS.IDENTIFIER, identifier);
        collectionMap.put(ProductCollection.COLLECTION_ARGS.MAX_ITEMS, DEFAULT_MAX_ITEMS);
        collectionMap.put(ProductCollection.COLLECTION_ARGS.OFFSET, DEFAULT_OFFSET);

        this.getProducts(collectionMap, null);
        @@@ STUBBED */
    }
}
