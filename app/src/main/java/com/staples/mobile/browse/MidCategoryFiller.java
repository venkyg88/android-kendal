package com.staples.mobile.browse;

import android.os.AsyncTask;
import android.util.Log;

import com.staples.mobile.JSONResponse;

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

    int fill(CategoryAdapter adapter, String path) {
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

        CategoryDetail category = response.Category[0];
        if (category==null) return(0);

        // Process subcategories
        if (category.subCategory!=null) {
            int count = category.subCategory.length;
            for (int i = 0; i < count; i++) {
                SubCategoryDetail detail = category.subCategory[i];
                String title = detail.description[0].name;
                if (title == null)
                    title = detail.description[0].text;
                CategoryItem item = new CategoryItem(title, detail.childCount, detail.categoryUrl);
                adapter.add(item);
            }
            Log.d(TAG, "Got " + count + " categories");
            return(count);
        }

        // Process filter groups
        if (category.filterGroup!=null) {
            int count = category.filterGroup.length;
            for (int i = 0; i < count; i++) {
                FilterGroup filterGroup = category.filterGroup[i];
                CategoryItem item = new CategoryItem(filterGroup.name, 0, null);
                adapter.add(item);
            }
            Log.d(TAG, "Got " + count + " filter groups");
            return(count);
        }

        return(0);
    }

    // Asynchronous task

    protected Integer doInBackground(CategoryFragment... fragment) {
        CategoryAdapter adapter = fragment[0].getAdapter();
        String path = fragment[0].getPath();

        int count = fill(adapter, path);

        adapter.update();
        return(count);
    }
}