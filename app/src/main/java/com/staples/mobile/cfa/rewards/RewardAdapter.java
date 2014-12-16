/*
 * Copyright (c) 2014 Staples, Inc. All rights reserved.
 */

package com.staples.mobile.cfa.rewards;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.staples.mobile.cfa.MainActivity;
import com.staples.mobile.cfa.R;
import com.staples.mobile.cfa.cart.CartItem;
import com.staples.mobile.cfa.widget.PriceSticker;
import com.staples.mobile.cfa.widget.QuantityEditor;
import com.staples.mobile.common.access.Access;
import com.staples.mobile.common.access.easyopen.api.EasyOpenApi;
import com.staples.mobile.common.access.easyopen.model.ApiError;
import com.staples.mobile.common.access.easyopen.model.EmptyResponse;
import com.staples.mobile.common.access.easyopen.model.cart.Coupon;
import com.staples.mobile.common.access.easyopen.model.member.Reward;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

//import com.staples.mobile.cfa.widget.QuantityEditor;


public class RewardAdapter extends ArrayAdapter<Reward> {

    private static final String TAG = RewardAdapter.class.getSimpleName();

    private MainActivity activity;
    private LayoutInflater inflater;
    private int rewardItemLayoutResId;

    // widget listeners
    private View.OnClickListener rewardAddDeleteButtonListener;



    public RewardAdapter(Activity activity, View.OnClickListener rewardAddDeleteButtonListener) {
        super(activity, R.layout.coupon_item_redeemable);
        rewardItemLayoutResId = R.layout.coupon_item_redeemable;
        this.activity = (MainActivity)activity;
        this.rewardAddDeleteButtonListener = rewardAddDeleteButtonListener;
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
        vh.couponAddDeleteButton = (Button) convertView.findViewById(R.id.reward_add_button);

        // set reward text
        vh.couponField1Vw.setText(reward.getAmount());
        vh.couponField2Vw.setText("exp " + reward.getExpiryDate());

        // set up applied state
        Resources r = activity.getResources();
        if (reward.isIsApplied()) {
            vh.couponAddDeleteButton.setText(r.getString(R.string.remove));
            int gray = r.getColor(R.color.text_gray);
            vh.couponField1Vw.setTextColor(gray);
            vh.couponField2Vw.setTextColor(gray);
        } else {
            vh.couponAddDeleteButton.setText(r.getString(R.string.add));
            int black = r.getColor(R.color.text_black);
            vh.couponField1Vw.setTextColor(black);
            vh.couponField2Vw.setTextColor(black);
        }

        vh.couponAddDeleteButton.setTag(position);

        // set widget listeners
        vh.couponAddDeleteButton.setOnClickListener(rewardAddDeleteButtonListener);

        return(convertView);
    }


    //---------------------------------------//
    //------------ inner classes ------------//
    //---------------------------------------//

    /************* view holder ************/

    static class ViewHolder {
        private TextView couponField1Vw;
        private TextView couponField2Vw;
        private Button couponAddDeleteButton;
    }
}
