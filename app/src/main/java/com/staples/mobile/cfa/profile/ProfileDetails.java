package com.staples.mobile.cfa.profile;

import android.text.TextUtils;
import android.util.Log;

import com.staples.mobile.cfa.login.LoginHelper;
import com.staples.mobile.common.access.Access;
import com.staples.mobile.common.access.easyopen.api.EasyOpenApi;
import com.staples.mobile.common.access.easyopen.model.ApiError;
import com.staples.mobile.common.access.easyopen.model.cart.Cart;
import com.staples.mobile.common.access.easyopen.model.cart.Coupon;
import com.staples.mobile.common.access.easyopen.model.cart.PaymentMethod;
import com.staples.mobile.common.access.easyopen.model.member.Address;
import com.staples.mobile.common.access.easyopen.model.member.CCDetails;
import com.staples.mobile.common.access.easyopen.model.member.InkRecyclingDetail;
import com.staples.mobile.common.access.easyopen.model.member.Member;
import com.staples.mobile.common.access.easyopen.model.member.MemberDetail;
import com.staples.mobile.common.access.easyopen.model.member.Reward;
import com.staples.mobile.common.access.easyopen.model.member.RewardDetail;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by Avinash Dodda.
 */
public class ProfileDetails implements Callback<MemberDetail> {
    private static final String TAG = ProfileDetails.class.getSimpleName();

    public interface ProfileRefreshCallback {
        public void onProfileRefresh(Member member, String errMsg);
    }

    public interface PaymentMethodSelectionListener {
        public void onPaymentMethodSelected(String id);
    }

    public interface AddressSelectionListener {
        public void onAddressSelected(String id);
    }


    // static cached member variable
    private static Member member;
    public static Member getMember() {
        return member;
    }
    public static void setMember(Member member) {
        ProfileDetails.member = member;
    }

    // other static data
    private static long mostRecentTimeRefreshRequested;
    public static PaymentMethodSelectionListener paymentMethodSelectionListener;
    public static AddressSelectionListener addressSelectionListener;
    public static String currentAddressId;
    public static String currentPaymentMethodId;


    // non-static instance data
    private EasyOpenApi easyOpenApi;
    private ProfileRefreshCallback callback; // must be non-static so that different areas of the app can call this simultaneously
    private Member memberUnderConstruction;
    private long timeRefreshRequested;

    /** calls API to get refreshed set of profile data */
    public void refreshProfile(ProfileRefreshCallback callback) {
        Access access = Access.getInstance();

        // handle case where not registered user
        if (!access.isLoggedIn() || access.isGuestLogin()) {
            if (callback != null) {
                callback.onProfileRefresh(null, null);
            }
            resetMember();
            return;
        }

        this.callback = callback;
        this.timeRefreshRequested = new Date().getTime(); // record when refresh request made to correctly handle simultaneous requests

        easyOpenApi = access.getEasyOpenApi(true);
        easyOpenApi.getMemberProfile(new Callback<MemberDetail>() {
            @Override
            public void success(MemberDetail memberDetail, Response response) {
                memberUnderConstruction = memberDetail.getMember().get(0);
                if (memberUnderConstruction.getStoredAddressCount() > 0) {
                    easyOpenApi.getMemberAddress(ProfileDetails.this);
                }
                if (memberUnderConstruction.getCreditCardCount() > 0) {
                    easyOpenApi.getMemberCreditCardDetails(ProfileDetails.this);
                }
                if (!TextUtils.isEmpty(memberUnderConstruction.getRewardsNumber()) && memberUnderConstruction.isRewardsNumberVerified()) {
                    easyOpenApi.getMemberRewardsDashboard(ProfileDetails.this);
                }
                finishMemberIfDone();
            }

            @Override
            public void failure(RetrofitError retrofitError) {
                String errMsg = ApiError.getErrorMessage(retrofitError);
                Log.i("Fail message when getting member details", " " + errMsg);
                Log.i("URl used to get member details", " " + retrofitError.getUrl());
                if (ProfileDetails.this.callback != null) {
                    ProfileDetails.this.callback.onProfileRefresh(null, errMsg);
                }
            }
        });

    }

    /** updates static member and notifies callback if new profile data complete */
    private void finishMemberIfDone() {
        if (memberUnderConstruction != null) {
            boolean addressesDone = memberUnderConstruction.getStoredAddressCount() == 0 ||
                    (memberUnderConstruction.getAddress() != null && memberUnderConstruction.getAddress().size() > 0);
            boolean paymentMethodsDone = memberUnderConstruction.getCreditCardCount() == 0 ||
                    (memberUnderConstruction.getCreditCard() != null && memberUnderConstruction.getCreditCard().size() > 0);
            boolean rewardsDone = TextUtils.isEmpty(memberUnderConstruction.getRewardsNumber()) || !memberUnderConstruction.isRewardsNumberVerified() ||
                    (memberUnderConstruction.getRewardDetails() != null && memberUnderConstruction.getRewardDetails().size() > 0);
            if (addressesDone && paymentMethodsDone && rewardsDone) {
                // if simultaneous refresh requests, only record profile returned from later request
                // (i.e. don't overwrite the response from a later request with a tardy response from
                // an earlier request). Notify the callback regardless.
                if (this.timeRefreshRequested > mostRecentTimeRefreshRequested) {
                    mostRecentTimeRefreshRequested = this.timeRefreshRequested;
                    member = memberUnderConstruction;
                }
                if (callback != null) {
                    callback.onProfileRefresh(member, null);
                }
            }
        }
    }

    /** implements Callback<MemberDetail> */
    public void success(MemberDetail memberDetail, Response response) {
        if (memberDetail.getMember() != null && memberDetail.getMember().size() > 0) {
            Member memberResponse = memberDetail.getMember().get(0);

            // if addresses response, set addresses
            if (memberResponse.getAddress() != null) {
                memberUnderConstruction.setAddress(memberResponse.getAddress());
            }

            // if credit cards response, set credit cards
            if (memberResponse.getCreditCard() != null) {
                memberUnderConstruction.setCreditCard(memberResponse.getCreditCard());
            }

            // if rewards response, set reward info
            if (memberResponse.getRewardDetails() != null) {
                memberUnderConstruction.setRewardDetails(memberResponse.getRewardDetails());
                memberUnderConstruction.setInkRecyclingDetails(memberResponse.getInkRecyclingDetails());
                memberUnderConstruction.setYearToDateSave(memberResponse.getYearToDateSave());
                memberUnderConstruction.setYearToDateSpend(memberResponse.getYearToDateSpend());
                memberUnderConstruction.setDisclaimerText(memberResponse.getDisclaimerText());
                memberUnderConstruction.setFooterBannerImage(memberResponse.getFooterBannerImage());
                memberUnderConstruction.setFooterBannerLink(memberResponse.getFooterBannerLink());
                memberUnderConstruction.setLastUpdate(memberResponse.getLastUpdate());
                memberUnderConstruction.setLogoImage(memberResponse.getLogoImage());
            }

            finishMemberIfDone();

        } else {
            Log.w(TAG, "empty MemberDetail returned"); // this can happen, need to determine why
            if (callback != null) {
                callback.onProfileRefresh(null, "Error loading profile");
            }
        }
    }

    /** implements Callback<MemberDetail> */
    public void failure(RetrofitError retrofitError) {
        String errMsg = ApiError.getErrorMessage(retrofitError);
        Log.i("Fail message when getting member details", " " + errMsg);
        Log.i("URl used to get member details", " " + retrofitError.getUrl());
        if (callback != null) {
            callback.onProfileRefresh(null, errMsg);
        }
    }

    public static void resetMember() {
        ProfileDetails.setMember(null);
    }

    public static boolean isRewardsMember() {
        if (member != null && member.getRewardsNumber() != null && member.isRewardsNumberVerified()) {
            return true;
        }
        return false;
    }

    /** returns true if profile as at least one address or at least one payment method */
    public static boolean hasAddress() {
        if (getMember() != null) {
            if (getMember().getStoredAddressCount() > 0) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasPaymentMethod() {
        if (getMember() != null) {
            if (getMember().getCreditCardCount() > 0) {
                return true;
            }
        }
        return false;
    }

    /** returns profile address matching specified addressId */
    public static Address getAddress(String addressId) {
        if (member != null && member.getAddress() != null) {
            for (Address address : member.getAddress()) {
                if (address.getAddressId().equals(addressId)) {
                    return address;
                }
            }
        }
        return null;
    }

    /** returns profile payment method matching specified paymentMethodId */
    public static CCDetails getPaymentMethod(String paymentMethodId) {
        if (member != null && member.getCreditCard() != null) {
            for (CCDetails creditCard : member.getCreditCard()) {
                if (creditCard.getCreditCardId().equals(paymentMethodId)) {
                    return creditCard;
                }
            }
        }
        return null;
    }

    /** extracts list of rewards from profile */
    public static List<Reward> getAllProfileRewards() {

        List<Reward> profileRewards = new ArrayList<Reward>();
        if (ProfileDetails.getMember() != null) {
            if (ProfileDetails.getMember().getInkRecyclingDetails() != null) {
                for (InkRecyclingDetail detail : ProfileDetails.getMember().getInkRecyclingDetails()) {
                    if (detail.getReward() != null) {
                        for (Reward reward : detail.getReward()) {
                            profileRewards.add(reward);
                        }
                    }
                }
            }
            if (ProfileDetails.getMember().getRewardDetails() != null) {
                for (RewardDetail detail : ProfileDetails.getMember().getRewardDetails()) {
                    if (detail.getReward() != null) {
                        for (Reward reward : detail.getReward()) {
                            profileRewards.add(reward);
                        }
                    }
                }
            }
        }
        return profileRewards;
    }

    /** rather than call the api to refresh the profile, use the info from the cart to update coupon info in the profile */
    public static void updateRewardsFromCart(Cart cart) {
        if (cart != null) {
            List<Reward> profileRewards = getAllProfileRewards();
            if (cart.getCoupon() != null && cart.getCoupon().size() > 0) {
                for (Coupon coupon : cart.getCoupon()) {
                    // coupon may or may not have a matching reward
                    Reward reward = findMatchingReward(profileRewards, coupon.getCode());
                    if (reward != null) {
                        reward.setIsApplied(true);
                        profileRewards.remove(reward); // remove the applied reward from the list
                    }
                }
            }
            // set remaining rewards to not applied
            for (Reward reward : profileRewards) {
                reward.setIsApplied(false);
            }
        }
    }

    /** returns reward within list that matches specified code, if any */
    public static Reward findMatchingReward(List<Reward> rewards, String code) {
        if (rewards != null) {
            for (Reward reward : rewards) {
                if (reward.getCode().equals(code)) {
                    return reward;
                }
            }
        }
        return null;
    }
}
