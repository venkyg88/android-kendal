package app.staples.mobile.cfa.cart;

import android.support.v7.widget.RecyclerView;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import app.staples.R;
import app.staples.mobile.cfa.widget.EditTextWithImeBackEvent;

public class CouponAdapter extends RecyclerView.Adapter<CouponAdapter.ViewHolder> {

    private static final String TAG = CouponAdapter.class.getSimpleName();

    private List<CouponItem> couponItems = new ArrayList<CouponItem>();

    // widget listeners
    private View.OnClickListener addButtonListener;
    private View.OnClickListener deleteButtonListener;
    private View.OnClickListener linkRewardsAccountButtonListener;
    PhoneNumberFormattingTextWatcher phoneNumberFormattingTextWatcher;


    public CouponAdapter(View.OnClickListener addButtonListener, View.OnClickListener deleteButtonListener,
                         View.OnClickListener linkRewardsAccountButtonListener) {
        this.addButtonListener = addButtonListener;
        this.deleteButtonListener = deleteButtonListener;
        this.linkRewardsAccountButtonListener = linkRewardsAccountButtonListener;
        this.phoneNumberFormattingTextWatcher = new PhoneNumberFormattingTextWatcher();
    }

    public void setItems(List<CouponItem> items) {
        couponItems = items;
        notifyDataSetChanged();
    }

    private int getLayoutId(int viewType) {
        switch (viewType) {
            case CouponItem.TYPE_ASSOC_REWARD_COUPON: return R.layout.coupon_item_associate_reward;
            case CouponItem.TYPE_COUPON_TO_ADD: return R.layout.coupon_item_add;
            case CouponItem.TYPE_APPLIED_COUPON: return R.layout.coupon_item_applied;
            case CouponItem.TYPE_REDEEMABLE_REWARD_HEADING: return R.layout.coupon_item_redeemable_heading;
            case CouponItem.TYPE_REDEEMABLE_REWARD: return R.layout.coupon_item_redeemable;
            case CouponItem.TYPE_NO_REDEEMABLE_REWARDS_MSG: return R.layout.coupon_item_no_rewards_msg;
            case CouponItem.TYPE_LINK_REWARD_ACCOUNT: return R.layout.rewards_linking;
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

        // if associate reward
        if (vh.assocRewardDescVw != null) {
            vh.assocRewardDescVw.setText(couponItem.getCouponShortDescription());
            vh.assocRewardAmountVw.setText(couponItem.getCouponAmountText());
        }

        // set up coupon code entry view
        if (vh.couponCodeEditVw != null) {
            vh.couponCodeEditVw.setText(couponItem.getCouponCodeToAdd());
            vh.couponCodeEditVw.setTag(position);
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
            couponItem.setCouponCodeVw(vh.couponCodeEditVw);
        }

        // set up link rewards account button
        if (vh.linkRewardsAccountButton != null) {
            vh.linkRewardsAccountButton.setTag(position);
            vh.linkRewardsAccountButton.setOnClickListener(linkRewardsAccountButtonListener);
            couponItem.setRewardsNumberVw(vh.rewardsNumberVw);
            couponItem.setRewardsPhoneNumberVw(vh.rewardsPhoneNumberVw);
            vh.rewardsPhoneNumberVw.addTextChangedListener(phoneNumberFormattingTextWatcher);
        }
    }


    //---------------------------------------//
    //------------ inner classes ------------//
    //---------------------------------------//

    /************* view holder ************/

    static class ViewHolder extends RecyclerView.ViewHolder {
        private EditTextWithImeBackEvent couponCodeEditVw;
        private TextView couponField1Vw;
        private TextView couponField2Vw;
        private TextView assocRewardDescVw;
        private TextView assocRewardAmountVw;
        private View couponDeleteButton;
        private View couponAddButton;
        private EditText rewardsNumberVw;
        private EditText rewardsPhoneNumberVw;
        private View linkRewardsAccountButton;

        /** constructor */
        public ViewHolder(View itemView, int type) {
            super(itemView);
            switch (type) {
                case CouponItem.TYPE_ASSOC_REWARD_COUPON:
                    assocRewardDescVw = (TextView) itemView.findViewById(R.id.associate_reward_desc);
                    assocRewardAmountVw = (TextView) itemView.findViewById(R.id.associate_reward_amount);
                    break;
                case CouponItem.TYPE_COUPON_TO_ADD:
                    couponCodeEditVw = (EditTextWithImeBackEvent) itemView.findViewById(R.id.coupon_code);
                    couponAddButton = itemView.findViewById(R.id.coupon_add_button);
                    break;
                case CouponItem.TYPE_APPLIED_COUPON:
                    couponField1Vw = (TextView) itemView.findViewById(R.id.coupon_amount);
                    couponField2Vw = (TextView) itemView.findViewById(R.id.coupon_expire);
                    couponDeleteButton = itemView.findViewById(R.id.coupon_delete_button);
                    break;
                case CouponItem.TYPE_REDEEMABLE_REWARD_HEADING:
                    break;
                case CouponItem.TYPE_REDEEMABLE_REWARD:
                    couponField1Vw = (TextView) itemView.findViewById(R.id.coupon_amount);
                    couponField2Vw = (TextView) itemView.findViewById(R.id.coupon_expire);
                    couponAddButton = itemView.findViewById(R.id.reward_add_button);
                    break;
                case CouponItem.TYPE_NO_REDEEMABLE_REWARDS_MSG:
                    break;
                case CouponItem.TYPE_LINK_REWARD_ACCOUNT:
                    linkRewardsAccountButton = itemView.findViewById(R.id.rewards_link_acct_button);
                    rewardsNumberVw = (EditText)itemView.findViewById(R.id.rewards_card_number);
                    rewardsPhoneNumberVw = (EditText)itemView.findViewById(R.id.rewards_phone_number);
                    break;
            }
        }
    }
}
