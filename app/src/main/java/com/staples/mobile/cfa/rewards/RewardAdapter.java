/*
 * Copyright (c) 2014 Staples, Inc. All rights reserved.
 */

package com.staples.mobile.cfa.rewards;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.staples.mobile.cfa.MainActivity;
import com.staples.mobile.cfa.R;
import com.staples.mobile.common.access.easyopen.model.member.Reward;


public class RewardAdapter extends ArrayAdapter<Reward> {

    private static final String TAG = RewardAdapter.class.getSimpleName();

    private MainActivity activity;
    private LayoutInflater inflater;
    private int rewardItemLayoutResId;

    // widget listeners
    private View.OnClickListener rewardButtonListener;



    public RewardAdapter(Activity activity, View.OnClickListener rewardButtonListener) {
        super(activity, R.layout.coupon_item_redeemable);
        rewardItemLayoutResId = R.layout.coupon_item_redeemable;
        this.activity = (MainActivity)activity;
        this.rewardButtonListener = rewardButtonListener;
        inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }



/* Views */


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        // use view holder pattern to improve listview performance
        ViewHolder vh = null;

        // Get a new or recycled view of the right type
        if (convertView == null) {
            convertView = inflater.inflate(rewardItemLayoutResId, parent, false);
            vh = new ViewHolder();
            convertView.setTag(vh);
        } else {
            vh = (ViewHolder) convertView.getTag();
        }

        Reward reward = getItem(position);

        vh.couponField1Vw = (TextView) convertView.findViewById(R.id.coupon_item_field1);
        vh.couponField2Vw = (TextView) convertView.findViewById(R.id.coupon_item_field2);
        vh.couponAddButton = (Button) convertView.findViewById(R.id.reward_add_button);
        vh.couponRemoveButton = (Button) convertView.findViewById(R.id.reward_remove_button);

        // set reward text
        vh.couponField1Vw.setText(reward.getAmount());
        vh.couponField2Vw.setText("exp " + reward.getExpiryDate());

        Resources r = activity.getResources();

        // set up applied state
        int blackTextColor = r.getColor(R.color.text_black);
        int grayTextColor = r.getColor(R.color.text_gray);
        if (reward.isIsApplied()) {
            vh.couponField1Vw.setTextColor(grayTextColor);
            vh.couponField2Vw.setTextColor(grayTextColor);
            vh.couponAddButton.setVisibility(View.GONE);
            vh.couponRemoveButton.setVisibility(View.VISIBLE);
        } else {
            vh.couponField1Vw.setTextColor(blackTextColor);
            vh.couponField2Vw.setTextColor(blackTextColor);
            vh.couponAddButton.setVisibility(View.VISIBLE);
            vh.couponRemoveButton.setVisibility(View.GONE);
        }

        vh.couponAddButton.setTag(position);
        vh.couponRemoveButton.setTag(position);

        // set widget listeners
        vh.couponAddButton.setOnClickListener(rewardButtonListener);
        vh.couponRemoveButton.setOnClickListener(rewardButtonListener);

        return(convertView);
    }


    //---------------------------------------//
    //------------ inner classes ------------//
    //---------------------------------------//

    /************* view holder ************/

    static class ViewHolder {
        private TextView couponField1Vw;
        private TextView couponField2Vw;
        private Button couponAddButton;
        private Button couponRemoveButton;
    }
}
