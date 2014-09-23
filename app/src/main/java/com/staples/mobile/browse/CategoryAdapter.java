package com.staples.mobile.browse;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.staples.mobile.EasyOpenApi;
import com.staples.mobile.MainApplication;
import com.staples.mobile.R;
import com.staples.mobile.browse.object.Browse;
import com.staples.mobile.browse.object.Category;
import com.staples.mobile.browse.object.Description;
import com.staples.mobile.browse.object.FilterGroup;
import com.staples.mobile.browse.object.Product;
import com.staples.mobile.browse.object.SubCategory;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class CategoryAdapter extends ArrayAdapter<CategoryItem> implements Callback<Browse> {
    private static final String TAG = "CategoryAdapter";

    private static final String RECOMMENDATION = "v1";
    private static final String STORE_ID = "10001";

    private static final String CATALOG_ID = "10051";
    private static final String LOCALE = "en_US";

    private static final String ZIPCODE = "01010";
    private static final String CLIENT_ID = "N6CA89Ti14E6PAbGTr5xsCJ2IGaHzGwS";

    private static final int MAXFETCH = 50;

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
            view = inflater.inflate(R.layout.category_item, parent, false);

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
        int i, j;

        MainApplication application = (MainApplication) activity.getApplication();
        EasyOpenApi easyOpenApi = application.getEasyOpenApi();

        // Get top categories
        if (path==null) {
            easyOpenApi.topCategories(RECOMMENDATION, STORE_ID, CATALOG_ID, LOCALE, null,
                                      ZIPCODE, CLIENT_ID, null, MAXFETCH, this);
            return;
        }

        // Decode category alphanumeric identifiers
        i = path.indexOf("/category/identifier/");
        if (i >= 0) {
            i += "/category/identifier/".length();
            j = path.indexOf('?', i);
            if (j <= 0) j = path.length();
            String identifier = path.substring(i, j);
            easyOpenApi.browseCategories(RECOMMENDATION, STORE_ID, identifier, CATALOG_ID, LOCALE,
                                         ZIPCODE, CLIENT_ID, null, MAXFETCH, this);
            return;
        }

        // Decode category numeric identifiers
        i = path.indexOf("parentIdentifier=");
        if (i >= 0) {
            i += "parentIdentifier=".length();
            j = path.indexOf('&', i);
            if (j <= 0) j = path.length();
            String parentIdentifier = path.substring(i, j);
            easyOpenApi.topCategories(RECOMMENDATION, STORE_ID, CATALOG_ID, LOCALE, parentIdentifier,
                                      ZIPCODE, CLIENT_ID, null, MAXFETCH, this);
            return;
        }

        // No idea what the path is
        Log.d(TAG, "Unknown path: "+path);
        notifyDataSetChanged();
    }

    public void success(Browse browse, Response response) {
        Category[] categories = browse.getCategory();
        if (categories==null || categories.length<1) {
            notifyDataSetChanged();
            return;
        }

        // Process categories
        if (categories.length>1) {
            int count = categories.length;
            for (int i = 0; i < count; i++) {
                Category category = categories[i];
                Description[] descriptions = category.getDescription1();
                if (descriptions != null && descriptions.length > 0) {
                    // Get category title
                    Description description = descriptions[0];
                    String title = description.getText();
                    if (title == null) title = description.getDescription();
                    if (title == null) title = description.getName();
                    CategoryItem item = new CategoryItem(title, category.getCategoryUrl(), category.getChildCount());
                    add(item);
                }
            }
            Log.d(TAG, "Got " + count + " categories");
            notifyDataSetChanged();
            return;
        }

        // Process subcategories
        Category category = categories[0];
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
                    CategoryItem item = new CategoryItem(title, subCategory.getCategoryUrl(), subCategory.getChildCount());
                    add(item);
                }
            }
            Log.d(TAG, "Got " + count + " subCategories");
            notifyDataSetChanged();
            return;
        }

        // Process products
        Product[] products = category.getProduct();
        if (products != null) {
            int count = products.length;
            for (int i = 0; i < count; i++) {
                Product product = products[i];
                CategoryItem item = new CategoryItem(product.getProductName(), null, 0);
                add(item);
            }
            Log.d(TAG, "Got " + count + " filter groups");
            notifyDataSetChanged();
            return;
        }

        notifyDataSetChanged();
    }

    public void failure(RetrofitError retrofitError) {
        Log.d(TAG, "Failure callback " + retrofitError);
        notifyDataSetChanged();
    }
}
