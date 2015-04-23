package com.staples.mobile.cfa.rewards;

import android.app.Fragment;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TabHost;
import android.widget.TextView;

import com.crittercism.app.Crittercism;
import com.staples.mobile.cfa.MainActivity;
import com.staples.mobile.cfa.R;
import com.staples.mobile.common.analytics.Tracker;
import com.staples.mobile.cfa.cart.CartApiManager;
import com.staples.mobile.cfa.profile.ProfileDetails;
import com.staples.mobile.cfa.util.CurrencyFormat;
import com.staples.mobile.cfa.widget.ActionBar;
import com.staples.mobile.cfa.widget.Numeric39Barcode;
import com.staples.mobile.common.access.easyopen.model.member.InkRecyclingDetail;
import com.staples.mobile.common.access.easyopen.model.member.Member;
import com.staples.mobile.common.access.easyopen.model.member.Reward;
import com.staples.mobile.common.access.easyopen.model.member.YearToDateSave;
import com.staples.mobile.common.access.easyopen.model.member.YearToDateSpend;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;

public class RewardsFragment extends Fragment implements View.OnClickListener, CartApiManager.CartRefreshCallback {
    private static final String TAG = RewardsFragment.class.getSimpleName();
    MainActivity activity;

    private TextView memberNameVw;
    private TextView memberDurationVw;
    private TextView rewardsNumberLabelVw;
    private Numeric39Barcode rewardsNumberBarcode39Vw;
    private TextView rewardsNumberVw;
    private RecyclerView rewardsListView;
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        Crittercism.leaveBreadcrumb("RewardsFragment:onCreateView(): Displaying the Rewards screen.");
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
        rewardsNumberBarcode39Vw = (Numeric39Barcode) view.findViewById(R.id.rewards_number_barcode39);
        rewardsNumberVw = (TextView) view.findViewById(R.id.rewards_number);

        // get rewards list views
        noRewardsMessageVw = view.findViewById(R.id.no_rewards_msg);
        rewardsListView = (RecyclerView) view.findViewById(R.id.rewards_list);
        rewardAdapter = new RewardAdapter(activity, this);
        rewardsListView.setAdapter(rewardAdapter);
        rewardsListView.setLayoutManager(new LinearLayoutManager(activity));
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
            // for now hide the text for which there's not yet api support
            memberDurationVw.setVisibility(View.GONE);
            rewardsNumberLabelVw.setVisibility(View.GONE);
//            memberDurationVw.setText("member since ???????????????");
//            rewardsNumberLabelVw.setText("type of rewards member ?????");
            rewardsNumberBarcode39Vw.setText(m.getRewardsNumber());
//            rewardsNumberBarcodeVw.setText("*"+m.getRewardsNumber()+"*");
//            rewardsNumberBarcodeVw.setTypeface(Typeface.createFromAsset(activity.getAssets(), "fonts/3of9_new.ttf"));
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

        DecimalFormat currencyFormat = CurrencyFormat.getFormatter();

        // set text of ink recycling views
        cartridgesRecycledVw.setText(""+cartridgesRecycled);
        cartridgesRecycledLabelVw.setText(r.getQuantityText(R.plurals.rewards_ink_recycled, cartridgesRecycled));
        cartridgesLimitVw.setText(String.format(r.getString(R.string.rewards_ink_limit), cartridgesLimit));
        cartridgesEarnedVw.setText(currencyFormat.format(cartridgesEarned));
        inkRewardsMessageVw.setText(Html.fromHtml(inkRewardsMessage)); // this strips html, but leaves info about the spanned hyperlinks which the textview uses to display them
        inkRewardsMessageVw.setMovementMethod(LinkMovementMethod.getInstance()); // this makes hyperlinks within the text clickable

        // set text of summary views
        totalYtdSavingsVw.setText(currencyFormat.format(totalYtdSavings));
        ytdSpendGoalVw.setText(currencyFormat.format(ytdSpendGoal));
        int progressMax = 100;
        ytdProgressBar.setMax(progressMax);
        ytdProgressBar.setProgress(ytdSpendGoal == 0? 0 : (int)Math.round(progressMax * ytdProgress / ytdSpendGoal));
        ytdMessageVw.setText(Html.fromHtml(ytdMessage)); // this strips html, but leaves info about the spanned hyperlinks which the textview uses to display them
        ytdMessageVw.setMovementMethod(LinkMovementMethod.getInstance()); // this makes hyperlinks within the text clickable

        return(view);
    }

    @Override
    public void onResume() {
        super.onResume();
        ActionBar.getInstance().setConfig(ActionBar.Config.REWARDS);
        Tracker.getInstance().trackStateForRewards(); // Analytics
    }

    @Override
    public void onClick(View view) {
        int position = (int)view.getTag();
        Reward reward = rewardAdapter.getRewards().get(position);
        switch(view.getId()) {
            case R.id.reward_add_button:
                showProgressIndicator();
                confirmationMsg = getResources().getString(R.string.rewards_addtocart_confirmation);
                CartApiManager.addCoupon(reward.getCode(), this);
                break;
            case R.id.reward_remove_button:
                showProgressIndicator();
                confirmationMsg = getResources().getString(R.string.rewards_removefromcart_confirmation);
                CartApiManager.deleteCoupon(reward.getCode(), this);
                break;
        }
    }

    private void fillRewardAdapter() {
        List<Reward> profileRewards = ProfileDetails.getAllProfileRewards();
        rewardAdapter.setRewards(profileRewards);
        rewardAdapter.notifyDataSetChanged();

        // set visibility of list vs. no-rewards msg
        noRewardsMessageVw.setVisibility((profileRewards.size() == 0)? View.VISIBLE:View.GONE);
        rewardsListView.setVisibility((profileRewards.size() > 0)? View.VISIBLE:View.GONE);
    }

    // when cart refresh done,
    public void onCartRefreshComplete(String errMsg) {
        hideProgressIndicator();
        ProfileDetails.updateRewardsFromCart(CartApiManager.getCart());
        fillRewardAdapter(); // note that error may occur on cart refresh rather than on coupon add/delete, so need to update rewards regardless
        if (errMsg != null) {
            activity.showErrorDialog(errMsg, false);
        } else {
            activity.showNotificationBanner(confirmationMsg);
        }
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
