package app.staples.mobile.cfa.cart;

import android.widget.EditText;

import app.staples.mobile.cfa.util.CurrencyFormat;

import com.staples.mobile.common.access.easyopen.model.cart.Coupon;
import com.staples.mobile.common.access.easyopen.model.member.Reward;

public class CouponItem {

    public static final int TYPE_COUPON_TO_ADD = 0;
    public static final int TYPE_APPLIED_COUPON = 1;
    public static final int TYPE_REDEEMABLE_REWARD_HEADING = 2;
    public static final int TYPE_REDEEMABLE_REWARD = 3;
    public static final int TYPE_NO_REDEEMABLE_REWARDS_MSG = 4;
    public static final int TYPE_ASSOC_REWARD_COUPON = 5;
    public static final int TYPE_LINK_REWARD_ACCOUNT = 6;

    private Coupon coupon;
    private Reward reward;
    private int itemType;
    private String couponCodeToAdd;
    private EditText couponCodeVw;
    private EditText rewardsNumberVw;
    private EditText rewardsPhoneNumberVw;

    public CouponItem(Coupon coupon, Reward reward, int itemType) {
        this.coupon = coupon;
        this.reward = reward;
        this.itemType = itemType;
    }

    public String getCouponField1Text() {
        if (reward != null) {
            return reward.getAmount();
        } else if (coupon != null) {
            return (reward != null)? reward.getAmount() :
                    (getCouponAmountText() + " off");
        }
        return "";
    }

    public String getCouponField2Text() {
        if (reward != null) {
            return "exp " + reward.getExpiryDate();
        } else if (coupon != null) {
            return "code: " + coupon.getCode();
        }
        return "";
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

    public Coupon getCoupon() {
        return coupon;
    }

    public void setCoupon(Coupon coupon) {
        this.coupon = coupon;
    }

    public Reward getReward() {
        return reward;
    }

    public void setReward(Reward reward) {
        this.reward = reward;
    }

    public int getItemType() {
        return itemType;
    }

    public void setItemType(int itemType) {
        this.itemType = itemType;
    }

    public String getCouponCodeToAdd() {
        return couponCodeToAdd;
    }

    public void setCouponCodeToAdd(String couponCodeToAdd) {
        this.couponCodeToAdd = couponCodeToAdd;
    }

    public EditText getCouponCodeVw() {
        return couponCodeVw;
    }

    public void setCouponCodeVw(EditText couponCodeVw) {
        this.couponCodeVw = couponCodeVw;
    }

    public EditText getRewardsNumberVw() {
        return rewardsNumberVw;
    }

    public void setRewardsNumberVw(EditText rewardsNumberVw) {
        this.rewardsNumberVw = rewardsNumberVw;
    }

    public EditText getRewardsPhoneNumberVw() {
        return rewardsPhoneNumberVw;
    }

    public void setRewardsPhoneNumberVw(EditText rewardsPhoneNumberVw) {
        this.rewardsPhoneNumberVw = rewardsPhoneNumberVw;
    }

}
