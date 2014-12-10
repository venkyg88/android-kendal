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
    Coupon coupon;
    Reward reward;

    NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();


    public CouponItem(Coupon coupon, Reward reward) {
        this.coupon = coupon;
        this.reward = reward;
    }

    public String getCouponField1Text() {
        if (coupon != null) {
            return (reward != null)? reward.getAmount() :
                    (currencyFormat.format(Math.abs(coupon.getAdjustedAmount())) + " off");
        }
        return "";
    }

    public String getCouponField2Text() {
        if (coupon != null) {
            return (reward != null)? ("exp " + reward.getExpiryDate()) : ("code: " + coupon.getCode());
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
}
