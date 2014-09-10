package com.staples.mobile.browse;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.staples.mobile.MainApplication;
import com.staples.mobile.R;
import com.staples.mobile.object.Browse;
import com.staples.mobile.object.Category;
import com.staples.mobile.object.Description;
import com.staples.mobile.object.FilterGroup;
import com.staples.mobile.object.SubCategory;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by pyhre001 on 8/20/14.
 */
public class CategoryAdapter extends ArrayAdapter<CategoryItem>
                             implements Callback<Browse> {
    private static final String TAG = "CategoryAdapter";

    private static final String RECOMMENDATION = "v1";
    private static final String STORE_ID = "10001";

    private static final String CATALOG_ID = "10051";
    private static final String LOCALE = "en_US";

    private static final String ZIPCODE = "01010";
    private static final String CLIENT_ID = "N6CA89Ti14E6PAbGTr5xsCJ2IGaHzGwS";

    private Activity activity;
    private LayoutInflater inflater;

    public CategoryAdapter(Activity activity) {
        super(activity, 0);
        this.activity = activity;
        inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        CategoryItem item = getItem(position);

        if (view==null)
            view = inflater.inflate(R.layout.category_item, null);

        TextView title = (TextView) view.findViewById(R.id.title);

        if (item.childCount > 0) {
            StringBuilder sb = new StringBuilder();
            sb.append(item.title);
            sb.append(" (");
            sb.append(item.childCount);
            sb.append(")");
            title.setText(sb.toString());
        } else title.setText(item.title);

        return(view);
    }

    void fill(String path) {
        RestAdapter easyOpenApi = ((MainApplication) activity.getApplication()).getEasyOpenApi();

        BrowseApi browseApi = easyOpenApi.create(BrowseApi.class);

        path = "category/identifier/SC2";
        browseApi.browse(RECOMMENDATION, STORE_ID, path, CATALOG_ID, LOCALE, ZIPCODE, CLIENT_ID, this);
    }

    public void success(Browse browse, Response response) {
        Category[] categories = browse.getCategory();
        if (categories!=null && categories.length>0) {
            Category category = categories[0];

            // Process subcategories
            SubCategory[] subCategories = category.getSubCategory();
            if (subCategories != null) {
                int count = subCategories.length;
                for (int i = 0; i < count; i++) {
                    SubCategory subCategory = subCategories[i];
                    Description[] descriptions = subCategory.getDescription();
                    if (descriptions != null && descriptions.length > 0) {
                        Description description = descriptions[0];
                        String title = description.getName();
                        if (title == null) title = description.getDescription();
                        CategoryItem item = new CategoryItem(title, subCategory.getChildCount(), subCategory.getCategoryUrl());
                        add(item);
                    }
                }
                Log.d(TAG, "Got " + count + " subCategories");
            }

            // Process filter groups
            FilterGroup[] filterGroups = category.getFilterGroup();
            if (filterGroups != null) {
                int count = filterGroups.length;
                for (int i = 0; i < count; i++) {
                    FilterGroup filterGroup = filterGroups[i];
                    CategoryItem item = new CategoryItem(filterGroup.getName(), 0, null);
                    add(item);
                }
                Log.d(TAG, "Got " + count + " filter groups");
            }
        }

        notifyDataSetChanged();
    }

    public void failure(RetrofitError retrofitError) {
        Log.d(TAG, "Sorry, failure " + retrofitError);
        notifyDataSetChanged();
    }
}
