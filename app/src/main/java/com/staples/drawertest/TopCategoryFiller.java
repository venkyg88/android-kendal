package com.staples.drawertest;

import android.os.AsyncTask;
import android.os.StrictMode;
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
import java.util.ArrayList;

/**
 * Created by pyhre001 on 8/15/14.
 */
public class TopCategoryFiller extends AsyncTask<DrawerAdapter, Void, Integer> {
    private static final String TAG = "TopCategoryFiller";

    private static final String API_STRING = "http://sapi.staples.com";
    private static final String CLIENT_ID = "JxP9wlnIfCSeGc9ifRAAGku7F4FSdErd";
    private static final String topCategoriesUrl = API_STRING +
                                                   "/v1/10001/category/top?catalogId=10051&zipCode=01010&client_id=" +
                                                   CLIENT_ID +
                                                   "&locale=en_US";

    static Gson gson = new Gson();

    public class JSONResponse {
        public CategoryDetail[] Category;
    }

    public class CategoryDetail {
        public Description[] description;
    }

    public class Description {
        public String text;
    }

    private InputStream retrieveStream(String url) {

        DefaultHttpClient client = new DefaultHttpClient();
        HttpGet getRequest = new HttpGet(url);

        try {

            HttpResponse getResponse = client.execute(getRequest);

            final int statusCode = getResponse.getStatusLine().getStatusCode();

            if (statusCode != HttpStatus.SC_OK && statusCode != HttpStatus.SC_BAD_REQUEST) {

                return null;
            }

            HttpEntity getResponseEntity = getResponse.getEntity();

            return getResponseEntity.getContent();
        } catch (IOException exIO) {

            exIO.printStackTrace();

            getRequest.abort();
        }

        return null;
    }

    protected Integer doInBackground(DrawerAdapter... adapter) {
        InputStream source = retrieveStream(topCategoriesUrl);
        InputStreamReader reader = new InputStreamReader(source);
        JSONResponse response = gson.fromJson(reader, JSONResponse.class);

        int count = response.Category.length;
        for(int i=0;i<count;i++) {
            String name = response.Category[i].description[0].text;
            adapter[0].addCategory(name);
        }
        adapter[0].update();
        Log.d(TAG, "Got " + count + " categories");
        return(count);
    }
}