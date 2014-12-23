/*
 * Copyright (c) 2014 Staples, Inc. All rights reserved.
 */

package com.staples.mobile.cfa.rewards;

import android.app.Fragment;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.staples.mobile.cfa.MainActivity;
import com.staples.mobile.cfa.R;
import com.staples.mobile.cfa.cart.CartFragment;
import com.staples.mobile.cfa.profile.ProfileDetails;
import com.staples.mobile.common.access.Access;
import com.staples.mobile.common.access.easyopen.api.EasyOpenApi;
import com.staples.mobile.common.access.easyopen.model.ApiError;
import com.staples.mobile.common.access.easyopen.model.EmptyResponse;
import com.staples.mobile.common.access.easyopen.model.cart.Coupon;
import com.staples.mobile.common.access.easyopen.model.member.InkRecyclingDetail;
import com.staples.mobile.common.access.easyopen.model.member.Member;
import com.staples.mobile.common.access.easyopen.model.member.Reward;
import com.staples.mobile.common.access.easyopen.model.member.YearToDateSave;
import com.staples.mobile.common.access.easyopen.model.member.YearToDateSpend;

import java.text.NumberFormat;
import java.util.Formatter;
import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class RewardsFragment extends Fragment implements View.OnClickListener, CartFragment.CartRefreshCallback {
    private static final String TAG = RewardsFragment.class.getSimpleName();
    MainActivity activity;

    private TextView memberNameVw;
    private TextView memberDurationVw;
    private TextView rewardsNumberLabelVw;
    private TextView rewardsNumberBarcodeVw;
    private TextView rewardsNumberVw;
    private ListView rewardsListView;
    private RewardAdapter rewardAdapter;
    private View noRewardsMessageVw;
    private TextView cartridgesRecycledVw;
    private TextView cartridgesRecycledLabelVw;
    private TextView cartridgesLimitVw;
    private TextView cartridgesEarnedVw;
    private TextView inkRewardsMessageVw;
    private TextView totalYtdSavingsVw;
    private TextView ytdSpendGoalVw;
    private ProgressBar ytdProgressBar;
    private TextView ytdMessageVw;

    private String confirmationMsg;

    NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        Resources r = getResources();
        activity = (MainActivity)getActivity();
        ViewGroup view = (ViewGroup) inflater.inflate(R.layout.rewards_fragment, container, false);

        // set up tabs
        TabHost tabHost = (TabHost) view.findViewById(android.R.id.tabhost);
        tabHost.setup();
        TabHost.TabSpec tab1 = tabHost.newTabSpec("First Tab");
        TabHost.TabSpec tab2 = tabHost.newTabSpec("Second Tab");
        TabHost.TabSpec tab3 = tabHost.newTabSpec("Third Tab");
        tab1.setIndicator(r.getString(R.string.rewards_list_tabtitle));
        tab1.setContent(R.id.tab1_rewards);
        tab2.setIndicator(r.getString(R.string.rewards_ink_tabtitle));
        tab2.setContent(R.id.tab2_ink_recycling);
        tab3.setIndicator(r.getString(R.string.rewards_summary_tabtitle));
        tab3.setContent(R.id.tab3_summary);
        tabHost.addTab(tab1);
        tabHost.addTab(tab2);
        tabHost.addTab(tab3);

        // get rewards member card views
        memberNameVw = (TextView) view.findViewById(R.id.member_name);
        memberDurationVw = (TextView) view.findViewById(R.id.member_duration);
        rewardsNumberLabelVw = (TextView) view.findViewById(R.id.rewards_number_label);
        rewardsNumberBarcodeVw = (TextView) view.findViewById(R.id.rewards_number_barcode);
        rewardsNumberVw = (TextView) view.findViewById(R.id.rewards_number);

        // get rewards list views
        noRewardsMessageVw = view.findViewById(R.id.no_rewards_msg);
        rewardsListView = (ListView) view.findViewById(R.id.rewards_list);
        rewardAdapter = new RewardAdapter(activity, this);
        rewardsListView.setAdapter(rewardAdapter);
        fillRewardAdapter();

        // set up ink recycling views
        cartridgesRecycledVw = (TextView) view.findViewById(R.id.cartridges_recycled);
        cartridgesRecycledLabelVw = (TextView) view.findViewById(R.id.cartridges_recycled_label);
        cartridgesLimitVw = (TextView) view.findViewById(R.id.cartridges_limit);
        cartridgesEarnedVw = (TextView) view.findViewById(R.id.cartridges_earned);
        inkRewardsMessageVw = (TextView) view.findViewById(R.id.ink_rewards_message);

        // set up summary views
        totalYtdSavingsVw = (TextView) view.findViewById(R.id.total_ytd_savings);
        ytdSpendGoalVw = (TextView) view.findViewById(R.id.ytd_spend_goal);
        ytdProgressBar = (ProgressBar) view.findViewById(R.id.ytd_progress);
        ytdMessageVw = (TextView) view.findViewById(R.id.ytd_message);


        // ink recycling fields
        int cartridgesRecycled = 0;
        int cartridgesLimit = 0;
        float cartridgesEarned = 0;
        String inkRewardsMessage = "";

        // summary fields
        float totalYtdSavings = 0;
        float ytdSpendGoal = 0;
        float ytdProgress = 0;
        String ytdMessage = "";


        // if profile info available
        Member m = ProfileDetails.getMember();
        if (m != null) {
            // membership card text
            memberNameVw.setText(m.getUserName());
            memberDurationVw.setText("member since ???????????????");
            rewardsNumberLabelVw.setText("type of rewards member ?????");
            rewardsNumberBarcodeVw.setText(m.getRewardsNumber());
            rewardsNumberBarcodeVw.setTypeface(Typeface.createFromAsset(activity.getAssets(), "fonts/3of9_new.ttf"));
            rewardsNumberVw.setText(m.getRewardsNumber());


            // if ink recycling info
            if (m.getInkRecyclingDetails() != null && m.getInkRecyclingDetails().size() > 0) {
                InkRecyclingDetail inkRecyclingDetail = m.getInkRecyclingDetails().get(0);
                cartridgesRecycled = inkRecyclingDetail.getInkCatridgesRecycled();
                cartridgesLimit = cartridgesRecycled + inkRecyclingDetail.getInkCatridgesRemaining();
                cartridgesEarned = inkRecyclingDetail.getInkRewardAmount();
                inkRewardsMessage = inkRecyclingDetail.getInkRewardsMessage();
            }
            // if ytd savings
            if (m.getYearToDateSave() != null && m.getYearToDateSave().size() > 0) {
                YearToDateSave yearToDateSave = m.getYearToDateSave().get(0);
                totalYtdSavings = yearToDateSave.getTotalSavings();
            }
            // if ytd spend
            if (m.getYearToDateSpend() != null && m.getYearToDateSpend().size() > 0) {
                YearToDateSpend yearToDateSpend = m.getYearToDateSpend().get(0);
                ytdSpendGoal = yearToDateSpend.getYtdBalanceAmount();
                ytdProgress = yearToDateSpend.getYtdSpendAmount();
                ytdMessage = yearToDateSpend.getYtdMessage();
            }
        }

        // set text of ink recycling views
        cartridgesRecycledVw.setText(""+cartridgesRecycled);
        cartridgesRecycledLabelVw.setText(r.getQuantityText(R.plurals.rewards_ink_recycled, cartridgesRecycled));
        cartridgesLimitVw.setText(String.format(r.getString(R.string.rewards_ink_limit), cartridgesLimit));
        cartridgesEarnedVw.setText(currencyFormat.format(cartridgesEarned));
        inkRewardsMessageVw.setText(inkRewardsMessage);

        // set text of summary views
        totalYtdSavingsVw.setText(currencyFormat.format(totalYtdSavings));
        ytdSpendGoalVw.setText(currencyFormat.format(ytdSpendGoal));
        int progressMax = 100;
        ytdProgressBar.setMax(progressMax);
        ytdProgressBar.setProgress(ytdSpendGoal == 0? 0 : (int)Math.round(progressMax * ytdProgress / ytdSpendGoal));
        ytdMessageVw.setText(ytdMessage);

        return(view);
    }

    @Override
    public void onResume() {
        super.onResume();
        activity.showActionBar(R.string.rewards_title, 0, null);
    }



    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.reward_add_button:
                int position = (int)view.getTag();
                Reward reward = rewardAdapter.getItem(position);
                showProgressIndicator();
                if (reward.isIsApplied()) {
                    confirmationMsg = getResources().getString(R.string.rewards_removefromcart_confirmation);
                    activity.removeCouponFromCart(reward.getCode(), this);
                } else {
                    confirmationMsg = getResources().getString(R.string.rewards_addtocart_confirmation);
                    activity.addCouponToCart(reward.getCode(), this);
                }
                break;
        }
    }

    private void fillRewardAdapter() {
        rewardAdapter.clear();
        List<Reward> profileRewards = ProfileDetails.getAllProfileRewards();
        for (Reward reward : profileRewards) {
            rewardAdapter.add(reward);
        }
        rewardAdapter.notifyDataSetChanged();

        // set visibility of list vs. no-rewards msg
        noRewardsMessageVw.setVisibility((profileRewards.size() == 0)? View.VISIBLE:View.GONE);
        rewardsListView.setVisibility((profileRewards.size() > 0)? View.VISIBLE:View.GONE);
    }

    // when cart refresh done,
    public void onCartRefreshComplete() {
        fillRewardAdapter();
        hideProgressIndicator();
        Toast.makeText(activity, confirmationMsg, Toast.LENGTH_LONG).show();
    }

    private void showProgressIndicator() {
        if (activity != null) {
            activity.showProgressIndicator();
        }
    }

    private void hideProgressIndicator() {
        if (activity != null) {
            activity.hideProgressIndicator();
        }
    }


}