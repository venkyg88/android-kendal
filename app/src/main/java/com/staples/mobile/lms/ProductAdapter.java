package com.staples.mobile.lms;

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
import com.staples.mobile.browse.CategoryItem;
import com.staples.mobile.browse.object.Browse;
import com.staples.mobile.browse.object.Category;
import com.staples.mobile.browse.object.Description;
import com.staples.mobile.browse.object.Product;
import com.staples.mobile.browse.object.SubCategory;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class ProductAdapter extends ArrayAdapter<ProductItem> implements Callback<Browse> {
    private static final String TAG = "ProductAdapter";

    private static final String RECOMMENDATION = "v1";
    private static final String STORE_ID = "10001";

    private static final String CATALOG_ID = "10051";
    private static final String LOCALE = "en_US";

    private static final String ZIPCODE = "01010";
    private static final String CLIENT_ID = "N6CA89Ti14E6PAbGTr5xsCJ2IGaHzGwS";

    private static final int MAXFETCH = 50;

    private Activity activity;
    private LayoutInflater inflater;

    public ProductAdapter(Activity activity) {
        super(activity, 0);
        this.activity = activity;
        inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
    @Override
    public View getView(int position, View view, ViewGroup parent) {
        ProductItem item = getItem(position);

        if (view==null)
            view = inflater.inflate(R.layout.category_item, parent, false);

        TextView title = (TextView) view.findViewById(R.id.title);

        title.setText(item.title);

        return(view);
    }

    void fill(String identifier) {
        MainApplication application = (MainApplication) activity.getApplication();
        EasyOpenApi easyOpenApi = application.getEasyOpenApi();
        easyOpenApi.browseCategories(RECOMMENDATION, STORE_ID, identifier, CATALOG_ID, LOCALE,
                                     ZIPCODE, CLIENT_ID, null, MAXFETCH, this);
    }

    public void success(Browse browse, Response response) {
        Category[] categories = browse.getCategory();
        if (categories==null || categories.length<1) {
            notifyDataSetChanged();
            return;
        }

        Product[] products = categories[0].getProduct();
        if (products != null) {
            int count = products.length;
            for (int i = 0; i < count; i++) {
                Product product = products[i];
                ProductItem item = new ProductItem(product.getProductName());
                add(item);
            }
            Log.d(TAG, "Got " + count + " products");
        }
        notifyDataSetChanged();
    }

    public void failure(RetrofitError retrofitError) {
        Log.d(TAG, "Failure callback " + retrofitError);
        notifyDataSetChanged();
    }
}
