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
        productContainer.limit = limit;
        productContainer.offset = offset;
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
                DEFAULT_LIMIT,
                DEFAULT_OFFSET,
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

        private static final String AMPERSAND = "&";
        private static final String COMMA = ",";
        private static final String EMPTY_STRING = "";
        private static final String EQUAL_SIGN = "=";
        private static final String QUESTION_MARK = "?";

        private static final String CATALOG_ID = "catalogId";
        private static final String CLIENT_ID = "client_id";
        private static final String FILTER_LIST = "filterList";
        private static final String JSON = "json";
        private static final String LOCALE = "locale";
        private static final String RESPONSE_FORMAT = "responseFormat";
        private static final String ZIP_CODE = "zipCode";

        // category/identifier/CL165566
        // category/identifier/CL165566?filterList=
        // category/identifier/CL165566?filterList=&limit=8&responseFormat=json

        // 10001/category/identifier/CL165566
        // 10001/category/identifier/CL165566?filterList=
        // 10001/category/identifier/CL165566?filterList=&limit=8&responseFormat=json

        public enum URL_TYPE {

            RAW,
            NO_PARAMETERS,
            FIRST_PARAMETER_ONLY,
            TWO_OR_MORE_PARAMETERS,
        }

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

        private URL_TYPE urlType = URL_TYPE.RAW;

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

            // category/identifier/CL165566?filterList=&limit=8&responseFormat=json
            // 10001/category/identifier/CL165566?filterList=&limit=8&responseFormat=json

            while (true) {

                if (urlExtension == null) {

                    errorCodes.add(ERROR_CODES.PARSE_NO_URL_EXTENSION);

                    handleParseError();

                    break; // while (true)
                }

                urlExtension = parseUrl(urlExtension);

                if (!errorCodes.isEmpty()) {

                    handleParseError();

                    break; // while (true)
                }

                Access access = Access.getInstance();

                EasyOpenApi easyOpenApi = access.getEasyOpenApi(false); // not secure

                easyOpenApi.getCategory(urlExtension,
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

        private String parseUrl(String urlExtension) {

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

            String newUrlExtension = urlExtension;

            newUrlExtension = insertPrefixData(newUrlExtension);

            identifyUrlType(newUrlExtension);

            if (urlType == URL_TYPE.NO_PARAMETERS) {

                newUrlExtension = addUrlParameter(newUrlExtension, QUESTION_MARK, FILTER_LIST, EMPTY_STRING);
            }

            newUrlExtension = setOffset(newUrlExtension);
            newUrlExtension = setLimit(newUrlExtension);

// TODO
//            newUrlExtension = addUrlParameter(newUrlExtension, AMPERSAND, DEFAULT_CATALOG_ID);
//            newUrlExtension = addUrlParameter(newUrlExtension, AMPERSAND, DEFAULT_LOCALE);
//            newUrlExtension = addUrlParameter(newUrlExtension, AMPERSAND, ZIP_CODE, DEFAULT_ZIP_CODE);
//            newUrlExtension = addUrlParameter(newUrlExtension, AMPERSAND, DEFAULT_CLIENT_ID);
//            newUrlExtension = addUrlParameter(newUrlExtension, AMPERSAND, RESPONSE_FORMAT, JSON);

            return (newUrlExtension.toString());
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
                if (terminatorIndex < 0) terminatorIndex = urlExtension.length();
                identifier = urlExtension.substring(valueIndex, terminatorIndex);

                break; // while (true)

            } // while (true)

            return (identifier);
        }

        private String insertPrefixData(String urlExtension) {

            // http://sapi.staples.com/v1/10001/category/identifier/BI739472?catalogId=10051&client_id=JxP9wlnIfCSeGc9ifRAAGku7F4FSdErd&sort=ratingAsc&locale=en_US&offset=1&zipCode=12345&limit=100

            // category/identifier/CL165566
            // category/identifier/CL165566?filterList=
            // category/identifier/CL165566?filterList=&limit=8&responseFormat=json

            // 10001/category/identifier/CL165566
            // 10001/category/identifier/CL165566?filterList=
            // 10001/category/identifier/CL165566?filterList=&limit=8&responseFormat=json

            if (LOGGING) Log.v(TAG, "ProductContainer:insertPrefixData():"
                            + " urlExtension[" + urlExtension + "]"
                            + " this[" + this + "]"
            );

            StringBuilder newUrlExtension = new StringBuilder(urlExtension);

            String prefix = DEFAULT_VERSION + "/";

            int argumentIndex = urlExtension.indexOf("category");

            if (argumentIndex == 0) {

                prefix += DEFAULT_STORE_ID + "/";
            }

            newUrlExtension.insert(0, prefix);

            return (newUrlExtension.toString());
        }

        private void identifyUrlType(String urlExtension) {

            // category/identifier/CL165566
            // category/identifier/CL165566?filterList=
            // category/identifier/CL165566?filterList=&limit=8&responseFormat=json

            // 10001/category/identifier/CL165566
            // 10001/category/identifier/CL165566?filterList=
            // 10001/category/identifier/CL165566?filterList=&limit=8&responseFormat=json

            if (LOGGING) Log.v(TAG, "ProductContainer:identifyUrlType():"
                            + " urlExtension[" + urlExtension + "]"
                            + " this[" + this + "]"
            );

            while (true) {

                int argumentIndex = urlExtension.indexOf(AMPERSAND);

                if (argumentIndex > 0) {
                    urlType = URL_TYPE.TWO_OR_MORE_PARAMETERS;
                    break; // while (true)
                }

                argumentIndex = urlExtension.indexOf(QUESTION_MARK);

                if (argumentIndex > 0) {
                    urlType = URL_TYPE.FIRST_PARAMETER_ONLY;
                    break; // while (true)
                }

                urlType = URL_TYPE.NO_PARAMETERS;

                break; // while (true)

            } // while (true)
        }

        private String addUrlParameter(String urlExtension,
                                       String parameterType,
                                       String parameterName,
                                       String parameterValue) {

            if (LOGGING) Log.v(TAG, "ProductContainer:addClientId():"
                            + " urlExtension[" + urlExtension + "]"
                            + " this[" + this + "]"
            );

            StringBuilder newUrlExtension = new StringBuilder(urlExtension);

            while (true) {

                // If the parameter already exists in the URL, do nothing.
                int argumentIndex = newUrlExtension.indexOf(parameterName);
                if (argumentIndex >= 0) break; // while (true)

                // Parameter not in URL. Add it.
                newUrlExtension.append(parameterType + parameterName + EQUAL_SIGN + parameterValue);

                break; // while (true)

            } // while (true)

            return (newUrlExtension.toString());
        }

        private String setOffset(String urlExtension) {

            // http://sapi.staples.com/v1/10001/category/identifier/BI739472?catalogId=10051&client_id=JxP9wlnIfCSeGc9ifRAAGku7F4FSdErd&sort=ratingAsc&locale=en_US&offset=1&zipCode=12345&limit=100

            // category/identifier/CL165566
            // category/identifier/CL165566?filterList=
            // category/identifier/CL165566?filterList=&limit=8&responseFormat=json

            // 10001/category/identifier/CL165566
            // 10001/category/identifier/CL165566?filterList=
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
                    String offset = (this.offset == null) ? DEFAULT_OFFSET : this.offset;
                    newUrlExtension.append(offset);
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
                if (terminatorIndex < 0) terminatorIndex = newUrlExtension.length();

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
                    String limit = (this.limit == null) ? DEFAULT_LIMIT : this.limit;
                    newUrlExtension.append(limit);
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
                if (terminatorIndex < 0) terminatorIndex = newUrlExtension.length();

                newUrlExtension.replace(valueIndex, terminatorIndex, this.limit);

                break; // while (true)

            } // while (true)

            return (newUrlExtension.toString());
        }

    } // ProductContainer
}
