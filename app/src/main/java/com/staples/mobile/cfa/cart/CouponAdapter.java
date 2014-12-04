/*
 * Copyright (c) 2014 Staples, Inc. All rights reserved.
 */

package com.staples.mobile.cfa.cart;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.staples.mobile.cfa.R;
import com.staples.mobile.common.access.easyopen.model.cart.Description;
import com.staples.mobile.common.access.easyopen.model.cart.Coupon;

import java.text.DecimalFormat;
import java.text.NumberFormat;


public class CouponAdapter extends ArrayAdapter<Coupon> {

    private static final String TAG = CouponAdapter.class.getSimpleName();

    private Activity activity;
    private LayoutInflater inflater;
    private int couponLayoutResId;

    NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();

    // widget listeners
    private View.OnClickListener deleteButtonListener;


    public CouponAdapter(Activity activity, int couponLayoutResId, View.OnClickListener deleteButtonListener) {
        super(activity, couponLayoutResId);
        this.activity = activity;
        this.couponLayoutResId = couponLayoutResId;
        this.deleteButtonListener = deleteButtonListener;
        inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }



/* Views */


    @Override
    public View getView(int position, View view, ViewGroup parent) {

        // use view holder pattern to improve listview performance
        ViewHolder vh = null;

        // Get a new or recycled view of the right type
        if (view == null) {
            view = inflater.inflate(couponLayoutResId, parent, false);
            vh = new ViewHolder(view); // get various widgets and place in view holder
            view.setTag(vh);
        } else {
            vh = (ViewHolder) view.getTag();
        }

        Coupon coupon = getItem(position);

        // set coupon text
        vh.couponDescriptionVw.setText(currencyFormat.format(Math.abs(coupon.getAdjustedAmount())) + " off");
        vh.couponCodeVw.setText("code: " + coupon.getCode());

        // associate item position with each widget
        vh.couponDeleteButton.setTag(position);
        vh.couponCodeVw.setTag(position);

        // set widget listeners
        vh.couponDeleteButton.setOnClickListener(deleteButtonListener);

        return(view);
    }

    //---------------------------------------//
    //------------ inner classes ------------//
    //---------------------------------------//

    /************* view holder ************/

    static class ViewHolder {
        private TextView couponCodeVw;
        private TextView couponDescriptionVw;
        private Button couponDeleteButton;

        ViewHolder(View convertView) {
            couponCodeVw = (TextView) convertView.findViewById(R.id.coupon_item_code);
            couponDescriptionVw = (TextView) convertView.findViewById(R.id.coupon_item_description);
            couponDeleteButton = (Button) convertView.findViewById(R.id.coupon_delete_button);
        }
    }
}
