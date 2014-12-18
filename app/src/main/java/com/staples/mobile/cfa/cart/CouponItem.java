/*
 * Copyright (c) 2014 Staples, Inc. All rights reserved.
 */

package com.staples.mobile.cfa.cart;

import com.staples.mobile.common.access.easyopen.model.cart.Coupon;
import com.staples.mobile.common.access.easyopen.model.member.Reward;

import java.text.NumberFormat;

/**
 * Created by sutdi001 on 12/9/14.
 */
public class CouponItem {

    public static final int TYPE_COUPON_TO_ADD = 0;
    public static final int TYPE_APPLIED_COUPON = 1;
    public static final int TYPE_REDEEMABLE_REWARD_HEADING = 2;
    public static final int TYPE_REDEEMABLE_REWARD = 3;
    public static final int TYPE_NO_REDEEMABLE_REWARDS_MSG = 4;
    public static final int TYPE_MAX_COUNT = TYPE_NO_REDEEMABLE_REWARDS_MSG + 1;

    private Coupon coupon;
    private Reward reward;
    private int itemType;
    private String couponCodeToAdd;

    private NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();


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
                    (currencyFormat.format(Math.abs(coupon.getAdjustedAmount())) + " off");
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
}
