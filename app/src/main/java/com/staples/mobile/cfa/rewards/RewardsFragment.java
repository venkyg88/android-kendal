/*
 * Copyright (c) 2014 Staples, Inc. All rights reserved.
 */

package com.staples.mobile.cfa.rewards;

import android.app.Fragment;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TabHost;
import android.widget.TextView;

import com.staples.mobile.cfa.MainActivity;
import com.staples.mobile.cfa.R;
import com.staples.mobile.cfa.profile.ProfileDetails;
import com.staples.mobile.common.access.easyopen.model.member.InkRecyclingDetail;
import com.staples.mobile.common.access.easyopen.model.member.Member;
import com.staples.mobile.common.access.easyopen.model.member.YearToDateSave;
import com.staples.mobile.common.access.easyopen.model.member.YearToDateSpend;

import java.text.NumberFormat;
import java.util.Formatter;

public class RewardsFragment extends Fragment {
    private static final String TAG = RewardsFragment.class.getSimpleName();
    MainActivity activity;

    private TextView cartridgesRecycledVw;
    private TextView cartridgesRecycledLabelVw;
    private TextView cartridgesLimitVw;
    private TextView cartridgesEarnedVw;
    private TextView inkRewardsMessageVw;
    private TextView totalYtdSavingsVw;
    private TextView ytdSpendGoalVw;
    private ProgressBar ytdProgressBar;
    private TextView ytdMessageVw;

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

        // get rewards list views


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

}
