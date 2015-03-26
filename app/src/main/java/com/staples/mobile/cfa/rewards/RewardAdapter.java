/*
 * Copyright (c) 2014 Staples, Inc. All rights reserved.
 */

package com.staples.mobile.cfa.rewards;

import android.app.Activity;
import android.content.res.Resources;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.staples.mobile.cfa.MainActivity;
import com.staples.mobile.cfa.R;
import com.staples.mobile.common.access.easyopen.model.member.Reward;

import java.util.List;


public class RewardAdapter extends RecyclerView.Adapter<RewardAdapter.ViewHolder> {

    private static final String TAG = RewardAdapter.class.getSimpleName();

    private MainActivity activity;
    private int rewardItemLayoutResId;

    List<Reward> rewards;

    // widget listeners
    private View.OnClickListener rewardButtonListener;

    /** constructor */
    public RewardAdapter(Activity activity, View.OnClickListener rewardButtonListener) {
        rewardItemLayoutResId = R.layout.coupon_item_redeemable;
        this.activity = (MainActivity)activity;
        this.rewardButtonListener = rewardButtonListener;
    }

/* Views */

    // Create new views (invoked by the layout manager)
    @Override
    public RewardAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(activity)
                .inflate(rewardItemLayoutResId, parent, false);
        return new ViewHolder(v);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder vh, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element

        Reward reward = rewards.get(position);

        // set reward text
        vh.couponField1Vw.setText(reward.getAmount());
        vh.couponField2Vw.setText("exp " + reward.getExpiryDate());

        Resources r = activity.getResources();

        // set up applied state
        int blackTextColor = r.getColor(R.color.staples_black);
        int grayTextColor = r.getColor(R.color.staples_middle_gray);
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
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return rewards.size();
    }


    public List<Reward> getRewards() {
        return rewards;
    }

    public void setRewards(List<Reward> rewards) {
        this.rewards = rewards;
    }


    //---------------------------------------//
    //------------ inner classes ------------//
    //---------------------------------------//

    /************* view holder ************/

//    static class ViewHolder {
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView couponField1Vw;
        private TextView couponField2Vw;
        private Button couponAddButton;
        private Button couponRemoveButton;

        /** constructor */
        public ViewHolder (View itemView) {
            super(itemView);
            couponField1Vw = (TextView) itemView.findViewById(R.id.coupon_item_field1);
            couponField2Vw = (TextView) itemView.findViewById(R.id.coupon_item_field2);
            couponAddButton = (Button) itemView.findViewById(R.id.reward_add_button);
            couponRemoveButton = (Button) itemView.findViewById(R.id.reward_remove_button);
        }
    }
}
