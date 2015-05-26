package app.staples.mobile.cfa.rewards;

import android.app.Fragment;
import android.content.res.Resources;
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
import com.staples.mobile.common.access.easyopen.model.member.InkRecyclingDetail;
import com.staples.mobile.common.access.easyopen.model.member.Member;
import com.staples.mobile.common.access.easyopen.model.member.Reward;
import com.staples.mobile.common.access.easyopen.model.member.YearToDateSave;
import com.staples.mobile.common.access.easyopen.model.member.YearToDateSpend;
import com.staples.mobile.common.analytics.Tracker;
import com.staples.mobile.common.widget.Code128CBarcode;

import java.text.DecimalFormat;
import java.util.List;

import app.staples.R;
import app.staples.mobile.cfa.DrawerItem;
import app.staples.mobile.cfa.MainActivity;
import app.staples.mobile.cfa.cart.CartApiManager;
import app.staples.mobile.cfa.profile.ProfileDetails;
import app.staples.mobile.cfa.util.CurrencyFormat;
import app.staples.mobile.cfa.widget.ActionBar;

public class RewardsFragment extends Fragment implements View.OnClickListener, CartApiManager.CartRefreshCallback {
    private static final String TAG = RewardsFragment.class.getSimpleName();

    private RewardAdapter adapter;
    private String confirmationMsg;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        Crittercism.leaveBreadcrumb("RewardsFragment:onCreateView(): Displaying the Rewards screen.");
        Resources res = getResources();
        MainActivity activity = (MainActivity) getActivity();
        ViewGroup view = (ViewGroup) inflater.inflate(R.layout.rewards_fragment, container, false);

        // set up tabs
        TabHost tabHost = (TabHost) view.findViewById(android.R.id.tabhost);
        tabHost.setup();
        TabHost.TabSpec tab1 = tabHost.newTabSpec("First Tab");
        TabHost.TabSpec tab2 = tabHost.newTabSpec("Second Tab");
        TabHost.TabSpec tab3 = tabHost.newTabSpec("Third Tab");
        tab1.setIndicator(res.getString(R.string.rewards_list_tabtitle));
        tab1.setContent(R.id.tab1_rewards);
        tab2.setIndicator(res.getString(R.string.rewards_ink_tabtitle));
        tab2.setContent(R.id.tab2_ink_recycling);
        tab3.setIndicator(res.getString(R.string.rewards_summary_tabtitle));
        tab3.setContent(R.id.tab3_summary);
        tabHost.addTab(tab1);
        tabHost.addTab(tab2);
        tabHost.addTab(tab3);

        // get rewards list views
        RecyclerView list = (RecyclerView) view.findViewById(R.id.rewards_list);
        adapter = new RewardAdapter(activity, this);
        list.setAdapter(adapter);
        list.setLayoutManager(new LinearLayoutManager(activity));
        fillRewardAdapter(view);

        // ink recycling fields
        int cartridgesRecycled = 0;
        int cartridgesLimit = 0;
        float cartridgesEarned = 0f;
        String inkRewardsMessage = null;

        // summary fields
        float totalYtdSavings = 0f;
        float ytdSpendGoal = 0f;
        float ytdProgress = 0f;
        String ytdMessage = null;

        // if profile info available
        Member member = ProfileDetails.getMember();
        if (member != null) {
            // membership card text
            ((TextView) view.findViewById(R.id.member_name)).setText(member.getUserName());
            // for now hide the text for which there's not yet api support
            view.findViewById(R.id.member_duration).setVisibility(View.GONE);
            view.findViewById(R.id.rewards_number_label).setVisibility(View.GONE);
//            memberDurationVw.setText("member since ???????????????");
//            rewardsNumberLabelVw.setText("type of rewards member ?????");
            ((Code128CBarcode) view.findViewById(R.id.rewards_number_barcode)).setText(member.getRewardsNumber());
            String caption = Code128CBarcode.formatCaption(member.getRewardsNumber(), ' ');
            ((TextView) view.findViewById(R.id.rewards_number)).setText(caption);

            // if ink recycling info
            if (member.getInkRecyclingDetails() != null && member.getInkRecyclingDetails().size() > 0) {
                InkRecyclingDetail inkRecyclingDetail = member.getInkRecyclingDetails().get(0);
                cartridgesRecycled = inkRecyclingDetail.getInkCatridgesRecycled();
                cartridgesLimit = cartridgesRecycled + inkRecyclingDetail.getInkCatridgesRemaining();
                cartridgesEarned = inkRecyclingDetail.getInkRewardAmount();
                inkRewardsMessage = inkRecyclingDetail.getInkRewardsMessage();
            }
            // if ytd savings
            if (member.getYearToDateSave() != null && member.getYearToDateSave().size() > 0) {
                YearToDateSave yearToDateSave = member.getYearToDateSave().get(0);
                totalYtdSavings = yearToDateSave.getTotalSavings();
            }
            // if ytd spend
            if (member.getYearToDateSpend() != null && member.getYearToDateSpend().size() > 0) {
                YearToDateSpend yearToDateSpend = member.getYearToDateSpend().get(0);
                ytdSpendGoal = yearToDateSpend.getYtdBalanceAmount();
                ytdProgress = yearToDateSpend.getYtdSpendAmount();
                ytdMessage = yearToDateSpend.getYtdMessage();
            }
        }

        DecimalFormat currencyFormat = CurrencyFormat.getFormatter();

        // set text of ink recycling views
        ((TextView) view.findViewById(R.id.cartridges_recycled)).setText(Integer.toString(cartridgesRecycled));
        ((TextView) view.findViewById(R.id.cartridges_recycled_label)).setText(res.getQuantityText(R.plurals.rewards_ink_recycled, cartridgesRecycled));
        ((TextView) view.findViewById(R.id.cartridges_limit)).setText(String.format(res.getString(R.string.rewards_ink_limit), cartridgesLimit));
        ((TextView) view.findViewById(R.id.cartridges_earned)).setText(currencyFormat.format(cartridgesEarned));

        if (inkRewardsMessage!=null) {
            TextView ctl = (TextView) view.findViewById(R.id.ink_rewards_message);
            ctl.setText(Html.fromHtml(inkRewardsMessage)); // this strips html, but leaves info about the spanned hyperlinks which the textview uses to display them
            ctl.setMovementMethod(LinkMovementMethod.getInstance()); // this makes hyperlinks within the text clickable
        }

        // set text of summary views
        ((TextView) view.findViewById(R.id.total_ytd_savings)).setText(currencyFormat.format(totalYtdSavings));
        ((TextView) view.findViewById(R.id.ytd_spend_goal)).setText(currencyFormat.format(ytdSpendGoal));

        int progressMax = 100;
        ProgressBar progress = (ProgressBar) view.findViewById(R.id.ytd_progress);
        progress.setMax(progressMax);
        progress.setProgress(ytdSpendGoal == 0 ? 0 : Math.round(progressMax * ytdProgress / ytdSpendGoal));

        if (ytdMessage!=null) {
            TextView ctl = (TextView) view.findViewById(R.id.ytd_message);
            ctl.setText(Html.fromHtml(ytdMessage)); // this strips html, but leaves info about the spanned hyperlinks which the textview uses to display them
            ctl.setMovementMethod(LinkMovementMethod.getInstance()); // this makes hyperlinks within the text clickable
        }

        return(view);
    }

    @Override
    public void onResume() {
        super.onResume();
        ActionBar.getInstance().setConfig(ActionBar.Config.REWARDS);
        Tracker.getInstance().trackStateForRewards(); // Analytics
    }

    private void fillRewardAdapter(View parent) {
        List<Reward> profileRewards = ProfileDetails.getAllProfileRewards();
        adapter.setRewards(profileRewards);
        adapter.notifyDataSetChanged();

        // set visibility of list vs. no-rewards msg
        parent.findViewById(R.id.no_rewards_msg).setVisibility((profileRewards.size() == 0) ? View.VISIBLE : View.GONE);
        parent.findViewById(R.id.rewards_list).setVisibility((profileRewards.size() > 0) ? View.VISIBLE : View.GONE);
    }

    // when cart refresh done,
    public void onCartRefreshComplete(String errMsg) {
        MainActivity activity = (MainActivity) getActivity();
        if (activity==null) return;

        activity.hideProgressIndicator();
        ProfileDetails.updateRewardsFromCart(CartApiManager.getCart());
        fillRewardAdapter(getView()); // note that error may occur on cart refresh rather than on coupon add/delete, so need to update rewards regardless
        if (errMsg != null) {
            activity.showErrorDialog(errMsg, false);
        } else {
            activity.showNotificationBanner(confirmationMsg);
        }
    }

    @Override
    public void onClick(View view) {
        Object tag;
        MainActivity activity = (MainActivity) getActivity();
        if (activity==null) return;

        switch(view.getId()) {
            case R.id.reward_add_button:
                tag = view.getTag();
                if (tag instanceof Reward) {
                    activity.showProgressIndicator();
                    confirmationMsg = getResources().getString(R.string.rewards_addtocart_confirmation);
                    CartApiManager.addCoupon(((Reward) tag).getCode(), this);
                }
                break;
            case R.id.reward_view_button:
                tag = view.getTag();
                if (tag instanceof Reward) {
                    Reward reward = (Reward) tag;
                    BarcodeFragment fragment = new BarcodeFragment();
                    fragment.setArguments("Coupon", reward.getCode(), reward.getAmount(), reward.getExpiryDate());
                    activity.selectFragment(DrawerItem.BARCODE, fragment, MainActivity.Transition.RIGHT);

                }
                break;
            case R.id.reward_remove_button:
                tag = view.getTag();
                if (tag instanceof Reward) {
                    activity.showProgressIndicator();
                    confirmationMsg = getResources().getString(R.string.rewards_removefromcart_confirmation);
                    CartApiManager.deleteCoupon(((Reward) tag).getCode(), this);
                }
                break;
        }
    }
}
