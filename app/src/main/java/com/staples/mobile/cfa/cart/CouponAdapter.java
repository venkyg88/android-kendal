/*
 * Copyright (c) 2014 Staples, Inc. All rights reserved.
 */

package com.staples.mobile.cfa.cart;

import android.app.Activity;
import android.content.Context;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.staples.mobile.cfa.R;
import com.staples.mobile.cfa.widget.EditTextWithImeBackEvent;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;


public class CouponAdapter extends BaseAdapter {

    private static final String TAG = CouponAdapter.class.getSimpleName();

    private Activity activity;
    private LayoutInflater inflater;
    private ArrayList<CouponItem> couponItems = new ArrayList<CouponItem>();

    // widget listeners
    private View.OnClickListener addButtonListener;
    private View.OnClickListener deleteButtonListener;


    public CouponAdapter(Activity activity, View.OnClickListener addButtonListener, View.OnClickListener deleteButtonListener) {
        this.activity = activity;
        this.addButtonListener = addButtonListener;
        this.deleteButtonListener = deleteButtonListener;
        inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void setItems(List<CouponItem> items) {
        couponItems.clear();
        couponItems.addAll(items);
        notifyDataSetChanged();
    }


    @Override
    public int getItemViewType(int position) {
        return getItem(position).getItemType();
    }

    @Override
    public int getViewTypeCount() {
        return CouponItem.TYPE_MAX_COUNT;
    }

    @Override
    public int getCount() {
        return couponItems.size();
    }

    @Override
    public CouponItem getItem(int position) {
        return couponItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


/* Views */


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        // use view holder pattern to improve listview performance
        ViewHolder vh = null;

        int type = getItemViewType(position);

        // Get a new or recycled view of the right type
        if (convertView == null) {
            vh = new ViewHolder();
            switch (type) {
                case CouponItem.TYPE_COUPON_TO_ADD:
                    convertView = inflater.inflate(R.layout.coupon_item_add, parent, false);
                    vh.couponCodeEditVw = (EditTextWithImeBackEvent) convertView.findViewById(R.id.coupon_code);
                    vh.couponAddButton = (Button) convertView.findViewById(R.id.coupon_add_button);
                    break;
                case CouponItem.TYPE_APPLIED_COUPON:
                    convertView = inflater.inflate(R.layout.coupon_item_applied, parent, false);
                    vh.couponField1Vw = (TextView) convertView.findViewById(R.id.coupon_item_field1);
                    vh.couponField2Vw = (TextView) convertView.findViewById(R.id.coupon_item_field2);
                    vh.couponDeleteButton = (Button) convertView.findViewById(R.id.coupon_delete_button);
                    break;
                case CouponItem.TYPE_REDEEMABLE_REWARD_HEADING:
                    convertView = inflater.inflate(R.layout.coupon_item_redeemable_heading, parent, false);
                    break;
                case CouponItem.TYPE_REDEEMABLE_REWARD:
                    convertView = inflater.inflate(R.layout.coupon_item_redeemable, parent, false);
                    vh.couponField1Vw = (TextView) convertView.findViewById(R.id.coupon_item_field1);
                    vh.couponField2Vw = (TextView) convertView.findViewById(R.id.coupon_item_field2);
                    vh.couponAddButton = (Button) convertView.findViewById(R.id.reward_add_button);
                    break;
                case CouponItem.TYPE_NO_REDEEMABLE_REWARDS_MSG:
                    convertView = inflater.inflate(R.layout.coupon_item_no_rewards_msg, parent, false);
                    break;
            }
            convertView.setTag(vh);
        } else {
            vh = (ViewHolder) convertView.getTag();
        }

        CouponItem couponItem = getItem(position);

        // set up coupon code entry view
        if (vh.couponCodeEditVw != null) {
            vh.couponCodeEditVw.setText(couponItem.getCouponCodeToAdd());
            vh.couponCodeEditVw.setTag(position);
            vh.couponCodeEditVw.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                    retrieveCouponCodeFromEditText(textView);
                    return false;
                }
            });
            vh.couponCodeEditVw.setOnImeBackListener(new EditTextWithImeBackEvent.EditTextImeBackListener() {
                @Override public void onImeBack(EditTextWithImeBackEvent view, String text) {
                retrieveCouponCodeFromEditText((TextView) view);
                }
            });
            vh.couponCodeEditVw.setOnKeyListener(new View.OnKeyListener() {
                @Override public boolean onKey(View v, int keyCode, KeyEvent event) {
                    retrieveCouponCodeFromEditText((TextView) v);
                    return false;
                }
            });
        }

        // set coupon text
        if (vh.couponField1Vw != null) {
            vh.couponField1Vw.setText(couponItem.getCouponField1Text());
            vh.couponField2Vw.setText(couponItem.getCouponField2Text());
        }

        // set up delete button
        if (vh.couponDeleteButton != null) {
            vh.couponDeleteButton.setTag(position);
            vh.couponDeleteButton.setOnClickListener(deleteButtonListener);
        }

        // set up add button
        if (vh.couponAddButton != null) {
            vh.couponAddButton.setTag(position);
            vh.couponAddButton.setOnClickListener(addButtonListener);
        }

        return(convertView);
    }

    private void retrieveCouponCodeFromEditText(TextView v) {
        CouponItem couponItem = getItem((Integer) v.getTag());
        couponItem.setCouponCodeToAdd(v.getText().toString());
    }

    //---------------------------------------//
    //------------ inner classes ------------//
    //---------------------------------------//

    /************* view holder ************/

    static class ViewHolder {
        private EditTextWithImeBackEvent couponCodeEditVw;
        private TextView couponField1Vw;
        private TextView couponField2Vw;
        private Button couponDeleteButton;
        private Button couponAddButton;
    }
}
