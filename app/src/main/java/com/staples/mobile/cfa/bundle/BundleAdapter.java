package com.staples.mobile.cfa.bundle;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.staples.mobile.R;
import com.staples.mobile.cfa.widget.DataWrapper;
import com.staples.mobile.cfa.widget.PriceSticker;
import com.staples.mobile.cfa.widget.RatingStars;
import com.staples.mobile.common.access.Access;
import com.staples.mobile.common.access.easyopen.api.EasyOpenApi;
import com.staples.mobile.common.access.easyopen.model.browse.*;

import java.util.List;

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
    private DataWrapper wrapper;
    private LayoutInflater inflater;
    private int layout;
    private Drawable noPhoto;

    public BundleAdapter(Activity activity, DataWrapper wrapper) {
        super(activity, 0);
        this.activity = activity;
        this.wrapper = wrapper;
        inflater = activity.getLayoutInflater();
        noPhoto = activity.getResources().getDrawable(R.drawable.no_photo);
    }

    public void setLayout(int layout) {
        this.layout = layout;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        BundleItem item = getItem(position);

        if (view==null)
            view = inflater.inflate(layout, parent, false);

        TextView title = (TextView) view.findViewById(R.id.title);
        if (title!=null) title.setText(item.title);

        RatingStars ratingStars = (RatingStars) view.findViewById(R.id.rating);
        ratingStars.setRating(item.customerRating, item.customerCount);

        PriceSticker priceSticker = (PriceSticker) view.findViewById(R.id.pricing);
        priceSticker.setPricing(item.price, item.unit);

        ImageView image = (ImageView) view.findViewById(R.id.image);
        if (item.imageUrl==null) image.setImageDrawable(noPhoto);
        else Picasso.with(activity).load(item.imageUrl).error(noPhoto).into(image);

        return(view);
    }

    void fill(String path) {
        int i, j;

        wrapper.setState(DataWrapper.State.LOADING);

        EasyOpenApi easyOpenApi = Access.getInstance().getEasyOpenApi(false);

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
        List<Category> categories = browse.getCategory();
        if (categories==null || categories.size()<1) {
            notifyDataSetChanged();
            return;
        }

        // Process products
        Category category = categories.get(0);
        List<Product> products = category.getProduct();
        if (products != null) {
            for (Product product : products) {
                BundleItem item = new BundleItem(product.getProductName(), product.getSku());
                item.setImageUrl(product.getImage());
                item.setPrice(product.getPricing());
                item.customerRating = product.getCustomerReviewRating();
                item.customerCount = product.getCustomerReviewCount();
                add(item);
            }
            wrapper.setState(DataWrapper.State.DONE);
            notifyDataSetChanged();
            return;
        }

        wrapper.setState(DataWrapper.State.EMPTY);
        notifyDataSetChanged();
    }

    @Override
    public void failure(RetrofitError retrofitError) {
        Log.d(TAG, "Failure callback " + retrofitError);
        wrapper.setState(DataWrapper.State.EMPTY);
        notifyDataSetChanged();
    }
}
