package com.staples.mobile.cfa.bundle;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;
import com.staples.mobile.R;
import com.staples.mobile.cfa.MainApplication;
import com.staples.mobile.cfa.widget.ListViewWrapper;
import com.staples.mobile.common.access.easyopen.api.EasyOpenApi;
import com.staples.mobile.common.access.easyopen.model.browse.Browse;
import com.staples.mobile.common.access.easyopen.model.browse.Category;
import com.staples.mobile.common.access.easyopen.model.browse.Product;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class BundleAdapter extends ArrayAdapter<BundleItem> implements Callback<Browse> {
    private static final String TAG = "BundleAdapter";

    private static final String RECOMMENDATION = "v1";
    private static final String STORE_ID = "10001";

    private static final String CATALOG_ID = "10051";
    private static final String LOCALE = "en_US";

    private static final String ZIPCODE = "01010";
    private static final String CLIENT_ID = "N6CA89Ti14E6PAbGTr5xsCJ2IGaHzGwS";

    private static final int MAXFETCH = 50;

    private Activity activity;
    private ListViewWrapper wrapper;
    private LayoutInflater inflater;
    private Picasso picasso;

    public BundleAdapter(Activity activity, ListViewWrapper wrapper) {
        super(activity, 0);
        this.activity = activity;
        this.wrapper = wrapper;
        inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        picasso = Picasso.with(activity);
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        BundleItem item = getItem(position);

        if (view==null)
            view = inflater.inflate(R.layout.bundle_item, parent, false);

        TextView title = (TextView) view.findViewById(R.id.title);
        if (title!=null) title.setText(item.title);

        ImageView image = (ImageView) view.findViewById(R.id.image);
        RequestCreator requestCreator = picasso.load(item.imageUrl);
        requestCreator.into(image);
        requestCreator.fit();

        return(view);
    }

    void fill(String path) {
        int i, j;

        wrapper.setState(ListViewWrapper.State.LOADING);

        MainApplication application = (MainApplication) activity.getApplication();
        EasyOpenApi easyOpenApi = application.getEasyOpenApi();

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

        // No idea what the path is
        Log.d(TAG, "Unknown path: " + path);
    }

    @Override
    public void success(Browse browse, Response response) {
        Category[] categories = browse.getCategory();
        if (categories==null || categories.length<1) {
            notifyDataSetChanged();
            return;
        }

        // Process products
        Category category = categories[0];
        Product[] products = category.getProduct();
        if (products != null) {
            int count = products.length;
            for (int i = 0; i < count; i++) {
                Product product = products[i];
                BundleItem item = new BundleItem(product.getProductName(), product.getSku());
                item.setImageUrl(product.getThumbnailImage());
                add(item);
            }
            Log.d(TAG, "Got " + count + " products");
            wrapper.setState(ListViewWrapper.State.DONE);
            notifyDataSetChanged();
            return;
        }

        wrapper.setState(ListViewWrapper.State.EMPTY);
        notifyDataSetChanged();
    }

    @Override
    public void failure(RetrofitError retrofitError) {
        Log.d(TAG, "Failure callback " + retrofitError);
        wrapper.setState(ListViewWrapper.State.EMPTY);
        notifyDataSetChanged();
    }
}
