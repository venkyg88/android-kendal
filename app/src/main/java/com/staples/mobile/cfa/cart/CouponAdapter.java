/*
 * Copyright (c) 2014 Staples, Inc. All rights reserved.
 */

package com.staples.mobile.cfa.cart;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.staples.mobile.cfa.R;

import java.text.NumberFormat;


public class CouponAdapter extends ArrayAdapter<CouponItem> {

    private static final String TAG = CouponAdapter.class.getSimpleName();

    private Activity activity;
    private LayoutInflater inflater;
    private int couponLayoutResId;

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

        CouponItem couponItem = getItem(position);

        // set coupon text
        vh.couponField1Vw.setText(couponItem.getCouponField1Text());
        vh.couponField2Vw.setText(couponItem.getCouponField2Text());

        // associate item position with each widget
        vh.couponDeleteButton.setTag(position);
//        vh.couponField2Vw.setTag(position);

        // set widget listeners
        vh.couponDeleteButton.setOnClickListener(deleteButtonListener);

        return(view);
    }

    //---------------------------------------//
    //------------ inner classes ------------//
    //---------------------------------------//

    /************* view holder ************/

    static class ViewHolder {
        private TextView couponField1Vw;
        private TextView couponField2Vw;
        private Button couponDeleteButton;

        ViewHolder(View convertView) {
            couponField1Vw = (TextView) convertView.findViewById(R.id.coupon_item_field1);
            couponField2Vw = (TextView) convertView.findViewById(R.id.coupon_item_field2);
            couponDeleteButton = (Button) convertView.findViewById(R.id.coupon_delete_button);
        }
    }
}
