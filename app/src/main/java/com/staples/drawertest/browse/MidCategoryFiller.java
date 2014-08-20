package com.staples.drawertest.browse;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.ArrayAdapter;

import com.staples.drawertest.JSONResponse;
import com.staples.drawertest.browse.CategoryFragment;

import org.apache.http.HttpStatus;

/**
 * Created by pyhre001 on 8/20/14.
 */
public class MidCategoryFiller extends AsyncTask<CategoryFragment, Void, Integer> {
    private static final String TAG = "MidCategoryFiller";

    public static class MidCategoryResponse extends JSONResponse {
        private CategoryDetail[] Category;
    }

    public static class CategoryDetail {
        private SubCategoryDetail[] subCategory;
        private int childCount;
    }

    public static class SubCategoryDetail {
        Description[] description;
        private int childCount;
        private String categoryUrl;
    }

    public static class Description {
        private String name;
        private String text;
    }

    protected Integer doInBackground(CategoryFragment... fragment) {
        String path = fragment[0].getPath();
        MidCategoryResponse response = (MidCategoryResponse) JSONResponse.getResponse(path, MidCategoryResponse.class);
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

        CategoryAdapter adapter = fragment[0].getAdapter();

        CategoryDetail category = response.Category[0];
        int count = category.subCategory.length;
        for(int i=0;i<count;i++) {
            SubCategoryDetail detail = category.subCategory[i];
            String title = detail.description[0].name;
            if (title==null)
                title = detail.description[0].text;
            Log.d(TAG, "SubCategory: "+title);
            adapter.addCategory(title);
        }
        adapter.update();
        Log.d(TAG, "Got " + count + " categories");
        return(count);
    }
}