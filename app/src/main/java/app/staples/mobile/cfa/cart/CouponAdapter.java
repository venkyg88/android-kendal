package app.staples.mobile.cfa.cart;

import android.support.v7.widget.RecyclerView;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import app.staples.R;
import app.staples.mobile.cfa.widget.DualHintEdit;

public class CouponAdapter extends RecyclerView.Adapter<CouponAdapter.ViewHolder> implements DualHintEdit.OnUpdatedTextListener {

    private static final String TAG = CouponAdapter.class.getSimpleName();

    private List<CouponItem> couponItems = new ArrayList<CouponItem>();

    // widget listeners
    private View.OnClickListener onClickListener;
    private PhoneNumberFormattingTextWatcher phoneNumberFormattingTextWatcher;

    public CouponAdapter(View.OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
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

    /* Views */

    @Override
    public CouponAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(getLayoutId(viewType), parent, false);
        ViewHolder vh = new ViewHolder(v, viewType);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder vh, int position) {
        CouponItem item = getItem(position);

        // if associate reward
        if (vh.assocRewardDescVw != null) {
            vh.assocRewardDescVw.setText(item.getCouponShortDescription());
        }
        if (vh.assocRewardAmountVw != null) {
            vh.assocRewardAmountVw.setText(item.getCouponAmountText());
        }

        // set up coupon code entry view
        if (vh.couponCodeEditVw != null) {
            vh.couponCodeEditVw.setTag(item);
            vh.couponCodeEditVw.setOnUpdatedTextListener(this);
            vh.couponCodeEditVw.setText(item.getCouponCodeToAdd());
        }

        // set coupon text
        if (vh.couponField1Vw != null) {
            vh.couponField1Vw.setText(item.getCouponField1Text());
        }
        if (vh.couponField2Vw != null) {
            vh.couponField2Vw.setText(item.getCouponField2Text());
        }

        // set up buttons
        if (vh.couponAddButton != null) {
            vh.couponAddButton.setTag(item);
            vh.couponAddButton.setOnClickListener(onClickListener);
        }
        if (vh.couponDeleteButton != null) {
            vh.couponDeleteButton.setTag(item);
            vh.couponDeleteButton.setOnClickListener(onClickListener);
        }
        if (vh.couponViewButton != null) {
            vh.couponViewButton.setTag(item);
            vh.couponViewButton.setOnClickListener(onClickListener);
        }

        // set up link rewards
        if (vh.linkRewardsAccountButton != null) {
            vh.linkRewardsAccountButton.setTag(item);
            vh.linkRewardsAccountButton.setOnClickListener(onClickListener);
        }
        if (vh.rewardsNumberVw != null) {
            vh.rewardsNumberVw.setTag(item);
            vh.rewardsNumberVw.setOnUpdatedTextListener(this);
            vh.rewardsNumberVw.setText(item.getRewardsNumber());
        }
        if (vh.phoneNumberVw != null) {
            vh.phoneNumberVw.setTag(item);
            vh.phoneNumberVw.setOnUpdatedTextListener(this);
            vh.phoneNumberVw.addTextChangedListener(phoneNumberFormattingTextWatcher);
            vh.phoneNumberVw.setText(item.getPhoneNumber());
        }
    }

    // This keeps the CouponItem up to date with text edits
    @Override
    public void onUpdatedText(DualHintEdit view) {
        Object tag = view.getTag();
        if (tag instanceof CouponItem) {
            CouponItem item = (CouponItem) tag;
            switch(view.getId()) {
                case R.id.coupon_code:
                    item.setCouponCodeToAdd(((TextView) view).getText().toString());
                    break;
                case R.id.rewards_card_number:
                    item.setRewardsNumber(((TextView) view).getText().toString());
                    break;
                case R.id.rewards_phone_number:
                    item.setPhoneNumber(((TextView) view).getText().toString());
                    break;
            }
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView couponField1Vw;
        private TextView couponField2Vw;
        private TextView assocRewardDescVw;
        private TextView assocRewardAmountVw;
        private View couponAddButton;
        private View couponDeleteButton;
        private View couponViewButton;
        private View linkRewardsAccountButton;
        private DualHintEdit couponCodeEditVw;
        private DualHintEdit rewardsNumberVw;
        private DualHintEdit phoneNumberVw;

        /** constructor */
        public ViewHolder(View itemView, int type) {
            super(itemView);
            switch (type) {
                case CouponItem.TYPE_ASSOC_REWARD_COUPON:
                    assocRewardDescVw = (TextView) itemView.findViewById(R.id.associate_reward_desc);
                    assocRewardAmountVw = (TextView) itemView.findViewById(R.id.associate_reward_amount);
                    break;
                case CouponItem.TYPE_COUPON_TO_ADD:
                    couponCodeEditVw = (DualHintEdit) itemView.findViewById(R.id.coupon_code);
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
                    couponAddButton = itemView.findViewById(R.id.coupon_add_button);
                    couponViewButton = itemView.findViewById(R.id.coupon_view_button);
                    break;
                case CouponItem.TYPE_NO_REDEEMABLE_REWARDS_MSG:
                    break;
                case CouponItem.TYPE_LINK_REWARD_ACCOUNT:
                    rewardsNumberVw = (DualHintEdit) itemView.findViewById(R.id.rewards_card_number);
                    phoneNumberVw = (DualHintEdit) itemView.findViewById(R.id.rewards_phone_number);
                    linkRewardsAccountButton = itemView.findViewById(R.id.rewards_link_acct_button);
                    break;
            }
        }
    }
}
