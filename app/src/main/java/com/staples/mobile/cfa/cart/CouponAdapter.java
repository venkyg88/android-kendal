/*
 * Copyright (c) 2014 Staples, Inc. All rights reserved.
 */

package com.staples.mobile.cfa.cart;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
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


public class CouponAdapter extends RecyclerView.Adapter<CouponAdapter.ViewHolder> {

    private static final String TAG = CouponAdapter.class.getSimpleName();

    private List<CouponItem> couponItems = new ArrayList<CouponItem>();

    // widget listeners
    private View.OnClickListener addButtonListener;
    private View.OnClickListener deleteButtonListener;


    public CouponAdapter(View.OnClickListener addButtonListener, View.OnClickListener deleteButtonListener) {
        this.addButtonListener = addButtonListener;
        this.deleteButtonListener = deleteButtonListener;
    }

    public void setItems(List<CouponItem> items) {
        couponItems = items;
        notifyDataSetChanged();
    }

    private int getLayoutId(int viewType) {
        switch (viewType) {
            case CouponItem.TYPE_COUPON_TO_ADD: return R.layout.coupon_item_add;
            case CouponItem.TYPE_APPLIED_COUPON: return R.layout.coupon_item_applied;
            case CouponItem.TYPE_REDEEMABLE_REWARD_HEADING: return R.layout.coupon_item_redeemable_heading;
            case CouponItem.TYPE_REDEEMABLE_REWARD: return R.layout.coupon_item_redeemable;
            case CouponItem.TYPE_NO_REDEEMABLE_REWARDS_MSG: return R.layout.coupon_item_no_rewards_msg;
        }
        return 0;
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return couponItems.size();
    }

    @Override
    public int getItemViewType(int position) {
        return getItem(position).getItemType();
    }


    public CouponItem getItem(int position) {
        return couponItems.get(position);
    }

    public List<CouponItem> getCouponItems() {
        return couponItems;
    }

    /* Views */

    // Create new views (invoked by the layout manager)
    @Override
    public CouponAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(getLayoutId(viewType), parent, false);
        ViewHolder vh = new ViewHolder(v, viewType);
        return vh;
    }


    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder vh, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element

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
    }


    private void retrieveCouponCodeFromEditText(TextView v) {
        CouponItem couponItem = getItem((Integer) v.getTag());
        couponItem.setCouponCodeToAdd(v.getText().toString());
    }

    //---------------------------------------//
    //------------ inner classes ------------//
    //---------------------------------------//

    /************* view holder ************/

    static class ViewHolder extends RecyclerView.ViewHolder {
        private EditTextWithImeBackEvent couponCodeEditVw;
        private TextView couponField1Vw;
        private TextView couponField2Vw;
        private Button couponDeleteButton;
        private Button couponAddButton;

        /** constructor */
        public ViewHolder(View itemView, int type) {
            super(itemView);
            switch (type) {
                case CouponItem.TYPE_COUPON_TO_ADD:
                    couponCodeEditVw = (EditTextWithImeBackEvent) itemView.findViewById(R.id.coupon_code);
                    couponAddButton = (Button) itemView.findViewById(R.id.coupon_add_button);
                    break;
                case CouponItem.TYPE_APPLIED_COUPON:
                    couponField1Vw = (TextView) itemView.findViewById(R.id.coupon_item_field1);
                    couponField2Vw = (TextView) itemView.findViewById(R.id.coupon_item_field2);
                    couponDeleteButton = (Button) itemView.findViewById(R.id.coupon_delete_button);
                    break;
                case CouponItem.TYPE_REDEEMABLE_REWARD_HEADING:
                    break;
                case CouponItem.TYPE_REDEEMABLE_REWARD:
                    couponField1Vw = (TextView) itemView.findViewById(R.id.coupon_item_field1);
                    couponField2Vw = (TextView) itemView.findViewById(R.id.coupon_item_field2);
                    couponAddButton = (Button) itemView.findViewById(R.id.reward_add_button);
                    break;
                case CouponItem.TYPE_NO_REDEEMABLE_REWARDS_MSG:
                    break;
            }
        }
    }
}
