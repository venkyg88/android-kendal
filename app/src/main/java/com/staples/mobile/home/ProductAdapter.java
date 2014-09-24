package com.staples.mobile.home;

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
import com.staples.mobile.easyopen.EasyOpenApi;
import com.staples.mobile.MainApplication;
import com.staples.mobile.R;
import com.staples.mobile.easyopen.Browse;
import com.staples.mobile.easyopen.Category;
import com.staples.mobile.easyopen.Product;

import retrofit.RetrofitError;
import retrofit.client.Response;

public class ProductAdapter extends ArrayAdapter<ProductItem>
                            implements retrofit.Callback<Browse>, com.squareup.picasso.Callback {
    private static final String TAG = "ProductAdapter";

    private static final int VIEW_BANNER = 0;
    private static final int VIEW_PRODUCT = 1;

    private static final String RECOMMENDATION = "v1";
    private static final String STORE_ID = "10001";

    private static final String CATALOG_ID = "10051";
    private static final String LOCALE = "en_US";

    private static final String ZIPCODE = "01010";
    private static final String CLIENT_ID = "N6CA89Ti14E6PAbGTr5xsCJ2IGaHzGwS";

    private static final int MAXFETCH = 50;

    private Activity activity;
    private LmsItem lmsItem;
    private LayoutInflater inflater;
    private Picasso picasso;

    public ProductAdapter(Activity activity, LmsItem lmsItem) {
        super(activity, 0);
        this.activity = activity;
        this.lmsItem = lmsItem;
        inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        picasso = Picasso.with(activity);
    }

        /* Views */

    @Override
    public int getViewTypeCount() {
        return (2);
    }

    @Override
    public int getItemViewType(int position) {
        if (position==0) return (VIEW_BANNER);
        else return (VIEW_PRODUCT);
    }

    @Override
    public boolean areAllItemsEnabled() {
        return(false);
    }

    @Override
    public boolean isEnabled(int position) {
        return(position>0);
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        ProductItem item = getItem(position);

        int type = getItemViewType(position);
        if (view==null) {
            switch(type) {
                case VIEW_BANNER:
                    view = inflater.inflate(R.layout.banner_item, parent, false);
                    ImageView banner = (ImageView) view.findViewById(R.id.image);
                    RequestCreator requestCreator = picasso.load(item.imageUrl);
                    requestCreator.into(banner, this);
                    requestCreator.fit();
                    break;
                case VIEW_PRODUCT:
                    view = inflater.inflate(R.layout.category_item, parent, false);
                    break;
            }
        }

        if (type==VIEW_PRODUCT) {
            TextView title = (TextView) view.findViewById(R.id.title);
            title.setText(item.title);
        }
        return(view);
    }

    void fill() {
        add(new ProductItem(null, lmsItem.bannerUrl, null));
        MainApplication application = (MainApplication) activity.getApplication();
        EasyOpenApi easyOpenApi = application.getEasyOpenApi();
        easyOpenApi.browseCategories(RECOMMENDATION, STORE_ID, lmsItem.identifier, CATALOG_ID, LOCALE,
                                     ZIPCODE, CLIENT_ID, null, MAXFETCH, this);
    }

    // Retrofit EasyOpen API call

    @Override
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
                ProductItem item = new ProductItem(product.getProductName(), null, product.getSku());
                add(item);
            }
            Log.d(TAG, "Got " + count + " products");
        }
        notifyDataSetChanged();
    }

    @Override
    public void failure(RetrofitError retrofitError) {
        Log.d(TAG, "Failure callback " + retrofitError);
        notifyDataSetChanged();
    }

    // Picasso

    @Override
    public void onSuccess() {
    }

    @Override
    public void onError() {
    }
}
