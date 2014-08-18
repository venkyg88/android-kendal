package com.staples.drawertest;

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

/**
 * Created by pyhre001 on 8/18/14.
 */
public abstract class JSONResponse {
    private static final String TAG = "JSONResponse";

    static Gson gson = new Gson();

    public int httpStatusCode;
    public JSONError errors[];

    public static class JSONError {
        public String errorMessage;
    }

    private static JSONResponse createDummyResponse(Class responseClass) {
        try
        {
            JSONResponse response = (JSONResponse) responseClass.newInstance();
            return (response);
        } catch (Exception e) {
            Log.e(TAG, "Can't instantiate dummy response " + e);
            return (null);
        }
    }

    public static JSONResponse getResponse (String url, Class responseClass) {
        HttpGet httpRequest;
        HttpResponse httpResponse;
        InputStreamReader reader;
        JSONResponse response;

        Log.d(TAG, "getResponse "+url);

        try { // TODO Slow network testing, remove for release!
            Thread.sleep(5000);
        } catch(Exception e) {}

        DefaultHttpClient client = new DefaultHttpClient();

        // Handle bad URL
        try {
            httpRequest = new HttpGet(url);
        } catch(IllegalArgumentException e) {
            response = createDummyResponse(responseClass);
            response.httpStatusCode = 991; // TODO needs a better way
            return(response);
        }

        // Handle basic IO errors
        try {
            httpResponse = client.execute(httpRequest);
        } catch (IOException e) {
            httpRequest.abort();
            response = createDummyResponse(responseClass);
            response.httpStatusCode = 992; // TODO needs a better way
            return(response);
        }

        // Handle HTTP errors
        int statusCode = httpResponse.getStatusLine().getStatusCode();
        if (statusCode!= HttpStatus.SC_OK &&
                statusCode!=500) // TODO Ugly acceptance of HTTP 500 errors
        {
            response = createDummyResponse(responseClass);
            response.httpStatusCode = statusCode;
            return(response);
        }

        // Handle empty entity
        HttpEntity httpEntity = httpResponse.getEntity();
        if (httpEntity==null) {
            response = createDummyResponse(responseClass);
            response.httpStatusCode = 993; // TODO needs a better way;
            return(response);
        }

        // Handle entity IO errors
        try {
            InputStream stream = httpEntity.getContent();
            reader = new InputStreamReader(stream);
        } catch(IOException e) {
            response = createDummyResponse(responseClass);
            response.httpStatusCode = 994; // TODO needs a better way;
            return(response);
        }

        response = (JSONResponse) gson.fromJson(reader, responseClass);
        response.httpStatusCode = statusCode;
        return(response);
    }
}
