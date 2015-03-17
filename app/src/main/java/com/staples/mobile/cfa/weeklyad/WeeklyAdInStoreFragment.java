/*
 * Copyright (c) 2015 Staples, Inc. All rights reserved.
 */

package com.staples.mobile.cfa.weeklyad;


import android.app.Fragment;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.staples.mobile.cfa.MainActivity;
import com.staples.mobile.cfa.R;
import com.staples.mobile.cfa.widget.ActionBar;
import com.staples.mobile.common.analytics.Tracker;


/**
 * Created by diana sutlief.
 */

public class WeeklyAdInStoreFragment extends Fragment {

    private static final String IMAGEURL = "imageUrl";
    private static final String TITLE = "title";
    private static final String PRICE = "price";

    private MainActivity activity;


    public void setArguments(String imageUrl, String title, String price) {
        Bundle args = new Bundle();
        args.putString(IMAGEURL, imageUrl);
        args.putString(TITLE, title);
        args.putString(PRICE, price);
        setArguments(args);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        activity = (MainActivity)getActivity();
        View view = inflater.inflate(R.layout.weekly_ad_in_store, container, false);

        String imageUrl = null;
        String title = null;
        String price = null;
        Bundle args = getArguments();
        if (args != null) {
            imageUrl = args.getString(IMAGEURL);
            title = args.getString(TITLE);
            price = args.getString(PRICE);
        }

        ImageView adImageVw = (ImageView)view.findViewById(R.id.weekly_ad_image);
        TextView titleVw = (TextView)view.findViewById(R.id.weekly_ad_title_text);
        TextView priceVw = (TextView)view.findViewById(R.id.weekly_ad_price);
        TextView priceExtensionVw = (TextView)view.findViewById(R.id.weekly_ad_price_extension);

        titleVw.setText(title);
        priceVw.setText(price);
        if (!TextUtils.isEmpty(price) && price.contains("$")) {
            priceExtensionVw.setVisibility(View.VISIBLE);
        }

        if (!TextUtils.isEmpty(imageUrl)) {
            Picasso.with(activity).load(imageUrl).into(adImageVw);
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

