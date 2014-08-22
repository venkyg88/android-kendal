package com.staples.drawertest;

import android.net.Uri;
import android.util.Log;
import com.google.gson.Gson;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;

/**
 * Created by pyhre001 on 8/18/14.
 */
public abstract class JSONResponse {
    private static final String TAG = "JSONResponse";

    static Gson gson = new Gson();

    private static final String API_STRING = "http://sapi.staples.com/v1";

    // US English
    private static final String CATALOG_ID = "10051";
    private static final String ZIPCODE = "01010";
    private static final String LOCALE = "en_US";

    // Canada French
//    private static final String CATALOG_ID = "20051";
//    private static final String ZIPCODE = "H3L1K7";
//    private static final String LOCALE = "fr_CA";

    private static final String CLIENT_ID = "N6CA89Ti14E6PAbGTr5xsCJ2IGaHzGwS";
//    private static final String CLIENT_ID = "ZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZ";

    public static HashMap<String, String> hashMap = new HashMap<String, String>();

    public transient int httpStatusCode;
    public JSONError errors[];

    public static class JSONError {
        public String errorMessage;
    }

    public static String buildUri(String path) {
        // Get basic URI
        Uri uri = Uri.parse(API_STRING+path);
        Uri.Builder builder = uri.buildUpon();

        // Add required parameters if not present
        if (uri.getQueryParameter("catalogId")==null)
            builder.appendQueryParameter("catalogId", CATALOG_ID);
        if (uri.getQueryParameter("zipCode")==null)
            builder.appendQueryParameter("zipCode", ZIPCODE);
        if (uri.getQueryParameter("locale")==null)
            builder.appendQueryParameter("locale", LOCALE);
        if (uri.getQueryParameter("client_id")==null)
            builder.appendQueryParameter("client_id", CLIENT_ID);

        return(builder.toString());
    }

    private static JSONResponse createErrorResponse(Class<? extends JSONResponse> responseClass, int error) {
        try
        {
            JSONResponse response = responseClass.newInstance();
            response.httpStatusCode = error;
            return (response);
        } catch (Exception e) {
            Log.e(TAG, "Can't instantiate error response " + e);
            return (null);
        }
    }

    public static JSONResponse getResponse (String path, Class<? extends JSONResponse> responseClass) {
        HttpGet httpRequest;
        HttpResponse httpResponse;
        InputStream stream;
        Reader reader;
        JSONResponse response;

        // Make URL
        String uri = buildUri(path);
        Log.d(TAG, "getResponse "+uri);

        // Check cache for hit
        String cache = hashMap.get(uri);
        if (cache!=null) {
            Log.d(TAG, "Cache hit");
            reader = new StringReader(cache);
            response = parseResponse(reader, responseClass, HttpStatus.SC_OK);
            return(response);
        }

//        try { // TODO Slow network testing, remove for release!
//            Thread.sleep(5000);
//        } catch(Exception e) {}

        // Handle bad URL
        try {
            httpRequest = new HttpGet(uri);
        } catch(IllegalArgumentException e) {
            response = createErrorResponse(responseClass, 991);
            return(response);
        }

        // Handle basic IO errors
        try {
            DefaultHttpClient client = new DefaultHttpClient();
            httpResponse = client.execute(httpRequest);
        } catch (Exception e) {
            httpRequest.abort();
            response = createErrorResponse(responseClass, 992);
            return(response);
        }

        // Handle HTTP errors
        int statusCode = httpResponse.getStatusLine().getStatusCode();
        if (statusCode!=HttpStatus.SC_OK &&
            statusCode!=HttpStatus.SC_INTERNAL_SERVER_ERROR) // TODO Ugly acceptance of HTTP 500 errors
        {
            response = createErrorResponse(responseClass, statusCode);
            return(response);
        }

        // Handle empty entity
        HttpEntity httpEntity = httpResponse.getEntity();
        if (httpEntity==null) {
            response = createErrorResponse(responseClass, 993);
            return(response);
        }

        // Handle entity IO errors
        try {
            stream = httpEntity.getContent();
            reader = new InputStreamReader(stream);
        } catch(IOException e) {
            response = createErrorResponse(responseClass, 994);
            return(response);
        }

        // Parse JSON
        response = parseResponse(reader, responseClass, statusCode);

        // Cache results
        if (response.httpStatusCode==HttpStatus.SC_OK) {
            String json = gson.toJson(response);
            if (json!=null)
                hashMap.put(uri, json);
        }

        return(response);
    }

    public static JSONResponse parseResponse(Reader reader, Class<? extends JSONResponse> responseClass,
                                             int statusCode) {
        JSONResponse response;

        // Handle parsing errors
        try {
            response = gson.fromJson(reader, responseClass);
        } catch (Exception e) {

            response = createErrorResponse(responseClass, 995);
            return (response);
        }

        // Success
        response.httpStatusCode = statusCode;
        return(response);
    }
}
