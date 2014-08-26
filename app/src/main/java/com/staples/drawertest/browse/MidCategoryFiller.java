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

    // JSON structure

    public static class MidCategoryResponse extends JSONResponse {
        private CategoryDetail[] Category;
    }

    private static class CategoryDetail {
        private SubCategoryDetail[] subCategory;
        private int childCount;
        private FilterGroup filterGroup[];
    }

    private static class SubCategoryDetail {
        Description[] description;
        private int childCount;
        private String categoryUrl;
    }

    private static class Description {
        private String name;
        private String text;
    }

    private static class FilterGroup {
        private String name;
    }

    // Asynchronous task

    protected Integer doInBackground(CategoryFragment... fragment) {
        String path = fragment[0].getPath();
        MidCategoryResponse response = (MidCategoryResponse) JSONResponse.getResponse(path, MidCategoryResponse.class);

        // Handle errors
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
        int count = 0;

        // Process subcategories
        if (category.subCategory!=null) {
            count = category.subCategory.length;
            for (int i = 0; i < count; i++) {
                SubCategoryDetail detail = category.subCategory[i];
                String title = detail.description[0].name;
                if (title == null)
                    title = detail.description[0].text;
                CategoryItem item = new CategoryItem(title, detail.childCount, detail.categoryUrl);
                adapter.add(item);
            }
            Log.d(TAG, "Got " + count + " categories");
        }

        // Process filter groups
        else if (category.filterGroup!=null) {
            count = category.filterGroup.length;
            for (int i = 0; i < count; i++) {
                FilterGroup filterGroup = category.filterGroup[i];
                CategoryItem item = new CategoryItem(filterGroup.name, 0, null);
                adapter.add(item);
            }
            Log.d(TAG, "Got " + count + " filter groups");
        }

        if (count>0) adapter.update();
        return(count);
    }
}