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

public class WeeklyAdInStoreFragment extends Fragment {

    private static final String TITLE = "title";
    private static final String PRICE = "price";
    private static final String UNIT = "unit";
    private static final String LITERAL = "literal";
    private static final String IMAGEURL = "imageUrl";
    private static final String INSTOREONLY = "inStoreOnly";

    public void setArguments(String title, float price, String unit, String literal, String imageUrl, boolean inStoreOnly) {
        Bundle args = new Bundle();
        args.putString(TITLE, title);
        args.putFloat(PRICE, price);
        args.putString(UNIT, unit);
        args.putString(LITERAL, literal);
        args.putString(IMAGEURL, imageUrl);
        args.putBoolean(INSTOREONLY, inStoreOnly);
        setArguments(args);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.weekly_ad_in_store, container, false);

        String title = null;
        Float price = 0.0f;
        String unit = null;
        String literal = null;
        String imageUrl = null;
        boolean inStoreOnly = false;
        Bundle args = getArguments();
        if (args != null) {
            title = args.getString(TITLE);
            price = args.getFloat(PRICE);
            unit = args.getString(UNIT);
            literal = args.getString(LITERAL);
            imageUrl = args.getString(IMAGEURL);
            inStoreOnly = args.getBoolean(INSTOREONLY);
        }

        TextView titleView = (TextView) view.findViewById(R.id.title);
        PriceSticker priceSticker = (PriceSticker) view.findViewById(R.id.pricing);
        ImageView imageView = (ImageView) view.findViewById(R.id.image);
        View availability = view.findViewById(R.id.availability);

        titleView.setText(title);
        if (literal!=null) {
            priceSticker.setLiterals(literal, null, null);
        } else {
            priceSticker.setPricing(price, 0.0f, unit, null);
        }
        if (imageUrl!=null) {
            Picasso.with(getActivity()).load(imageUrl).error(R.drawable.no_photo).into(imageView);
        }
        availability.setVisibility(inStoreOnly ? View.VISIBLE : View.GONE);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        ActionBar.getInstance().setConfig(ActionBar.Config.WEEKLYAD);
        Tracker.getInstance().trackStateForWeeklyAd(); // Analytics
    }
}

