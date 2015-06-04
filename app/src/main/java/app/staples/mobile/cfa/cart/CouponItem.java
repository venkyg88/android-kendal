package app.staples.mobile.cfa.cart;

import android.widget.EditText;

import app.staples.mobile.cfa.util.CurrencyFormat;

import com.staples.mobile.common.access.easyopen.model.cart.Coupon;
import com.staples.mobile.common.access.easyopen.model.member.Reward;

public class CouponItem {

    public static final int TYPE_COUPON_TO_ADD             = 0;
    public static final int TYPE_APPLIED_COUPON            = 1;
    public static final int TYPE_REDEEMABLE_REWARD_HEADING = 2;
    public static final int TYPE_REDEEMABLE_REWARD         = 3;
    public static final int TYPE_NO_REDEEMABLE_REWARDS_MSG = 4;
    public static final int TYPE_ASSOC_REWARD_COUPON       = 5;
    public static final int TYPE_LINK_REWARD_ACCOUNT       = 6;

    private int itemType;
    private Coupon coupon;
    private Reward reward;

    // Editable string storage
    private String couponCodeToAdd;
    private String rewardsNumber;
    private String phoneNumber;

    public CouponItem(int itemType, Coupon coupon, Reward reward) {
        this.coupon = coupon;
        this.reward = reward;
        this.itemType = itemType;
    }

    public String getCode() {
        if (couponCodeToAdd != null) return(couponCodeToAdd);
        if (reward != null) return(reward.getCode());
        if (coupon != null) return(coupon.getCode());
        return(null);
    }

    public String getCouponField1Text() {
        if (reward != null) return(reward.getAmount());
        if (coupon != null) return(getCouponAmountText() + " off");
        return(null);
    }

    public String getCouponField2Text() {
        if (reward != null) return("exp " + reward.getExpiryDate());
        if (coupon != null) return("code: " + coupon.getCode());
        return(null);
    }

    public String getCouponAmountText() {
        return CurrencyFormat.getFormatter().format(Math.abs(coupon.getAdjustedAmount()));
    }

    public String getCouponShortDescription() {
        if (coupon.getDescription() != null && coupon.getDescription().size() > 0) {
            return coupon.getDescription().get(0).getShortDescription();
        }
        return null;
    }

    // Plain getters

    public int getItemType() {
        return itemType;
    }

    public Coupon getCoupon() {
        return coupon;
    }

    public Reward getReward() {
        return reward;
    }

    // Editable string storage

    public String getCouponCodeToAdd() {
        return couponCodeToAdd;
    }

    public void setCouponCodeToAdd(String couponCodeToAdd) {
        this.couponCodeToAdd = couponCodeToAdd;
    }

    public String getRewardsNumber() {
        return rewardsNumber;
    }

    public void setRewardsNumber(String rewardsNumber) {
        this.rewardsNumber = rewardsNumber;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}
