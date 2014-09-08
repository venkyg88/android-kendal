package com.staples.drawertest;

import android.os.AsyncTask;
import android.util.Log;

import com.staples.drawertest.browse.CategoryFragment;

import org.apache.http.HttpStatus;

/**
 * Created by pyhre001 on 8/15/14.
 */
public class TopCategoryFiller extends AsyncTask<DrawerAdapter, Void, Integer> {
    private static final String TAG = "TopCategoryFiller";

    private static final String TOPCATEGORYPATH = "/10001/category/top";

    // JSON structure

    public static class TopCategoryResponse extends JSONResponse{
        private CategoryDetail[] Category;
    }

    private static class CategoryDetail {
        private Description[] description;
        private int childCount;
        private String categoryUrl;
    }

    private static class Description {
        private String name;
        private String text;
    }

    // Asynchronous task

    protected Integer doInBackground(DrawerAdapter... adapter) {
        TopCategoryResponse response = (TopCategoryResponse) JSONResponse.getResponse(TOPCATEGORYPATH, TopCategoryResponse.class);
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
            String title = detail.description[0].name;
            if (title == null)
                title = detail.description[0].text;
            DrawerItem item = adapter[0].findItemByTitle(title);
            if (item != null) {
                item.childCount = detail.childCount;
                item.path = detail.categoryUrl;
            }
        }
        adapter[0].update();
        Log.d(TAG, "Got " + count + " categories");
        return(count);
    }
}
