package com.staples.drawertest;

import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpStatus;

/**
 * Created by pyhre001 on 8/15/14.
 */
public class TopCategoryFiller extends AsyncTask<DrawerAdapter, Void, Integer> {
    private static final String TAG = "TopCategoryFiller";

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

    private static final String topCategoriesUrl = API_STRING +
                                                   "/10001/category/top" +
                                                   "?catalogId=" + CATALOG_ID +
                                                   "&zipCode=" + ZIPCODE +
                                                   "&locale=" + LOCALE +
                                                   "&client_id=" + CLIENT_ID;

    public static class TopCategoryResponse extends JSONResponse{
        public CategoryDetail[] Category;
    }

    public static class CategoryDetail {
        public Description[] description;
        public int childCount;
    }

    public static class Description {
        public String text;
    }

    protected Integer doInBackground(DrawerAdapter... adapter) {
        TopCategoryResponse response = (TopCategoryResponse) JSONResponse.getResponse(topCategoriesUrl, TopCategoryResponse.class);
        if (response==null) {
            Log.d(TAG, "JSONResponse was null");
            return(0);
        }
        if (response.httpStatusCode!=HttpStatus.SC_OK &&
                response.httpStatusCode!=HttpStatus.SC_INTERNAL_SERVER_ERROR) // TODO Ugly acceptance of HTTP 500 errors
        {
            Log.d(TAG, "HTTP returned "+response.httpStatusCode);
            return(0);
        }
        if (response.errors!=null) {
            Log.d(TAG, "API returned "+response.errors[0].errorMessage);
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