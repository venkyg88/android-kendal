package com.staples.drawertest;

import android.os.AsyncTask;
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
 * Created by pyhre001 on 8/15/14.
 */
public class TopCategoryFiller extends AsyncTask<DrawerAdapter, Void, Integer> {
    private static final String TAG = "TopCategoryFiller";

    private static final String API_STRING = "http://sapi.staples.com";
    private static final String CLIENT_ID = "N6CA89Ti14E6PAbGTr5xsCJ2IGaHzGwS";
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
        public int childCount;
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
Log.d(TAG, "Bad HTTP response");
                return null;
            }

            HttpEntity getResponseEntity = getResponse.getEntity();

            return getResponseEntity.getContent();
        } catch (IOException e) {
Log.d(TAG, "IO Exception "+e);

            getRequest.abort();
        }

        return(null);
    }

    protected Integer doInBackground(DrawerAdapter... adapter) {
        JSONResponse response;

        try {
            InputStream source = retrieveStream(topCategoriesUrl);
if (source==null)
    Log.d(TAG, "Null source");

            InputStreamReader reader = new InputStreamReader(source);
            response = gson.fromJson(reader, JSONResponse.class);
        }
        catch(Exception e) {
Log.d(TAG, "Error "+e);
            return(0);
        }

        int count = response.Category.length;
        for(int i=0;i<count;i++) {
            CategoryDetail detail = response.Category[i];
            StringBuilder name = new StringBuilder(256);
            name.append(detail.description[0].text);
            if (detail.childCount>0) {
                name.append(" (");
                name.append(detail.childCount);
                name.append(")");
            }
            adapter[0].addCategory(name.toString());
        }
        adapter[0].update();
        Log.d(TAG, "Got " + count + " categories");
        return(count);
    }
}