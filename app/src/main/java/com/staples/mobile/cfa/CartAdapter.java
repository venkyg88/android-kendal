/*
 * Copyright (c) 2014 Staples, Inc. All rights reserved.
 */

package com.staples.mobile.cfa;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.staples.mobile.R;
import com.staples.mobile.cfa.widget.PriceSticker;
import com.staples.mobile.common.access.Access;
import com.staples.mobile.common.access.easyopen.api.EasyOpenApi;
import com.staples.mobile.common.access.easyopen.model.browse.Browse;
import com.staples.mobile.common.access.easyopen.model.browse.Category;
import com.staples.mobile.common.access.easyopen.model.browse.Pricing;

import java.util.ArrayList;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class CartAdapter extends BaseAdapter implements Callback<Browse> {

    private static final String TAG = "CartAdapter";

    private static final String RECOMMENDATION = "v1";
    private static final String STORE_ID = "10001";

    private static final String CATALOG_ID = "10051";
    private static final String LOCALE = "en_US";

    private static final String ZIPCODE = "01010";
    private static final String CLIENT_ID = "N6CA89Ti14E6PAbGTr5xsCJ2IGaHzGwS";

    private static final int MAXFETCH = 50;

    private Activity activity;
    private LayoutInflater inflater;

    private ArrayList<CartItem> cartItems;

    private Drawable noPhoto;


    public CartAdapter(Activity activity) {
        super();
        this.activity = activity;
        noPhoto = activity.getResources().getDrawable(R.drawable.no_photo);
        inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        cartItems = new ArrayList<CartItem>();
    }


    // Items

    public int getTotalCount() {
        int totalCount = 0;
        for (CartItem item : cartItems) {
            totalCount += item.getQuantity();
        }
        return totalCount;
    }

    @Override
    public int getCount() {
        return cartItems.size();
    }

    @Override
    public long getItemId(int position) {
        return(getItem(position).hashCode());
    }

    @Override
    public CartItem getItem(int position) {
        if (position < cartItems.size()) {
            return(cartItems.get(position));
        }
        return(null);
    }

    /* Views */


    @Override
    public View getView(int position, View view, ViewGroup parent) {

        // Get a new or recycled view of the right type
        if (view == null) {
            view = inflater.inflate(R.layout.cart_item, parent, false);
        }

        CartItem item = getItem(position);

        // Set image
        ImageView imageView = (ImageView) view.findViewById(R.id.cartitem_image);
        if (item.getThumbnailImage() == null) {
            imageView.setImageDrawable(noPhoto);
        } else {
            Picasso.with(activity).load(item.getThumbnailImage().getUrl()).error(noPhoto).into(imageView);
        }

        // Set title
        TextView titleTextView = (TextView) view.findViewById(R.id.cartitem_title);
        titleTextView.setText(item.getDescription());

        // set price
        PriceSticker priceSticker = (PriceSticker) view.findViewById(R.id.cartitem_price);
        Pricing pricing = item.getPricing();
        if (pricing != null) {
            priceSticker.setPrice(pricing.getFinalPrice(), pricing.getUnitOfMeasure()); // pricing.getFinalPrice()
        }

        // set quantity
        TextView qtyTextView = (TextView) view.findViewById(R.id.cartitem_qty);
        qtyTextView.setText(""+item.getQuantity());


        return(view);
    }


    void fill() {
        EasyOpenApi easyOpenApi = Access.getInstance().getEasyOpenApi(false);

        // TODO: query for items in cart


        // temporary: getting data from browse request
        String identifier = "CL161546";
        easyOpenApi.browseCategories(RECOMMENDATION, STORE_ID, identifier, CATALOG_ID, LOCALE,
                ZIPCODE, CLIENT_ID, null, MAXFETCH, this);

        notifyDataSetChanged();
    }

    @Override
    public void success(Browse browse, Response response) {

        // TODO: get items returned from cart API

        // temporary: getting data from browse request
        Category[] categories = browse.getCategory();
        if (categories==null || categories.length<1) {
            notifyDataSetChanged();
            return;
        }
        Category category = categories[0];
        com.staples.mobile.common.access.easyopen.model.browse.Product[] products = category.getProduct();
        if (products != null) {
            for (com.staples.mobile.common.access.easyopen.model.browse.Product product : products) {
                cartItems.add(new CartItem(product, 1));
            }
            notifyDataSetChanged();
            return;
        }


        notifyDataSetChanged();
    }

    @Override
    public void failure(RetrofitError retrofitError) {
        Log.d(TAG, "Failure callback " + retrofitError);
        notifyDataSetChanged();
    }
}
