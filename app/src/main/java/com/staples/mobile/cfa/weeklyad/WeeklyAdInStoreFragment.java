/*
 * Copyright (c) 2015 Staples, Inc. All rights reserved.
 */

package com.staples.mobile.cfa.weeklyad;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.staples.mobile.cfa.R;
import com.staples.mobile.cfa.widget.ActionBar;
import com.staples.mobile.cfa.widget.PriceSticker;
import com.staples.mobile.common.analytics.Tracker;


/**
 * Created by diana sutlief.
 */

public class WeeklyAdInStoreFragment extends Fragment {

    private static final String TITLE = "title";
    private static final String IMAGEURL = "imageUrl";
    private static final String PRICE = "price";

    public void setArguments(String title, String imageUrl, float price) {
        Bundle args = new Bundle();
        args.putString(TITLE, title);
        args.putString(IMAGEURL, imageUrl);
        args.putFloat(PRICE, price);
        setArguments(args);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.weekly_ad_in_store, container, false);

        String title = null;
        String imageUrl = null;
        Float price = 0.0f;
        Bundle args = getArguments();
        if (args != null) {
            title = args.getString(TITLE);
            imageUrl = args.getString(IMAGEURL);
            price = args.getFloat(PRICE);
        }

        ImageView imageView = (ImageView) view.findViewById(R.id.image);
        TextView titleView = (TextView) view.findViewById(R.id.title);
        PriceSticker priceSticker = (PriceSticker) view.findViewById(R.id.pricing);

        titleView.setText(title);
        priceSticker.setPricing(price, 0.0f, null, null);

        if (imageUrl!=null) {
            Picasso.with(getActivity()).load(imageUrl).error(R.drawable.no_photo).into(imageView);
        }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        ActionBar.getInstance().setConfig(ActionBar.Config.WEEKLYAD);
        Tracker.getInstance().trackStateForWeeklyAd(); // Analytics
    }
}

