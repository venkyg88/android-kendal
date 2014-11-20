package com.staples.mobile.cfa.profile;

import android.util.Log;

import com.staples.mobile.cfa.login.LoginHelper;
import com.staples.mobile.common.access.Access;
import com.staples.mobile.common.access.easyopen.api.EasyOpenApi;
import com.staples.mobile.common.access.easyopen.model.ApiError;
import com.staples.mobile.common.access.easyopen.model.member.Address;
import com.staples.mobile.common.access.easyopen.model.member.CCDetails;
import com.staples.mobile.common.access.easyopen.model.member.Member;
import com.staples.mobile.common.access.easyopen.model.member.MemberDetail;

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

    public interface ProfileRefreshCallback {
        public void onProfileRefresh(Member member);
    }

    private static final String RECOMMENDATION = "v1";
    private static final String STORE_ID = "10001";
    private static final String CLIENT_ID = LoginHelper.CLIENT_ID;
    private static final String LOCALE = "en_US";

    // static cached member variable
    private static Member member;
    public static Member getMember() {
        return member;
    }
    public static void setMember(Member member) {
        ProfileDetails.member = member;
    }
    private static long mostRecentTimeRefreshRequested;

    // non-static instance data
    private EasyOpenApi easyOpenApi;
    private ProfileRefreshCallback callback; // must be non-static so that different areas of the app can call this simultaneously
    private Member memberUnderConstruction;
    private long timeRefreshRequested;

    /** calls API to get refreshed set of profile data */
    public void refreshProfile(ProfileRefreshCallback callback) {
        this.callback = callback;
        this.timeRefreshRequested = new Date().getTime(); // record when refresh request made to correctly handle simultaneous requests

        easyOpenApi = Access.getInstance().getEasyOpenApi(true);
        easyOpenApi.getMemberProfile(RECOMMENDATION, STORE_ID, LOCALE, CLIENT_ID, new Callback<MemberDetail>() {
            @Override
            public void success(MemberDetail memberDetail, Response response) {
                memberUnderConstruction = memberDetail.getMember().get(0);
                if (memberUnderConstruction.getStoredAddressCount() > 0) {
                    easyOpenApi.getMemberAddress(RECOMMENDATION, STORE_ID, LOCALE, CLIENT_ID, ProfileDetails.this);
                }
                if (memberUnderConstruction.getCreditCardCount() > 0) {
                    easyOpenApi.getMemberCreditCardDetails(RECOMMENDATION, STORE_ID, LOCALE, CLIENT_ID, ProfileDetails.this);
                }
                finishMemberIfDone();
            }

            @Override
            public void failure(RetrofitError retrofitError) {
                Log.i("Fail message when getting member details", " " + ApiError.getErrorMessage(retrofitError));
                Log.i("URl used to get member details", " " + retrofitError.getUrl());
                if (ProfileDetails.this.callback != null) {
                    ProfileDetails.this.callback.onProfileRefresh(null);
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
            if (addressesDone & paymentMethodsDone) {
                // if simultaneous refresh requests, only record profile returned from later request
                // (i.e. don't overwrite the response from a later request with a tardy response from
                // an earlier request). Notify the callback regardless.
                if (this.timeRefreshRequested > mostRecentTimeRefreshRequested) {
                    mostRecentTimeRefreshRequested = this.timeRefreshRequested;
                    member = memberUnderConstruction;
                }
                if (callback != null) {
                    callback.onProfileRefresh(member);
                }
            }
        }
    }

    /** implements Callback<MemberDetail> */
    public void success(MemberDetail memberDetail, Response response) {
        Member memberResponse = memberDetail.getMember().get(0);

        List<Address> addresses = memberResponse.getAddress();
        if (addresses!=null) {
            memberUnderConstruction.setAddress(memberResponse.getAddress());
        }

        List<CCDetails> creditCards = memberResponse.getCreditCard();
        if(creditCards !=null) {
            memberUnderConstruction.setCreditCard(memberResponse.getCreditCard());
        }

        finishMemberIfDone();
    }

    /** implements Callback<MemberDetail> */
    public void failure(RetrofitError retrofitError) {
        Log.i("Fail message when getting member details", " " + ApiError.getErrorMessage(retrofitError));
        Log.i("URl used to get member details", " " + retrofitError.getUrl());
        if (callback != null) {
            callback.onProfileRefresh(null);
        }
    }


//    public static void mergeMember(Member member) {
//
//        if(ProfileDetails.member == null){
//            ProfileDetails.member = member;
//        } else {
//            if(member.getUserName()!=null){
//                member.setCreditCard(ProfileDetails.member.getCreditCard());
//                member.setAddress(ProfileDetails.member.getAddress());
//                ProfileDetails.member = member;
//            }
//
//            if(member.getAddress()!=null){
//                ProfileDetails.member.setAddress(member.getAddress());
//            }
//
//            if(member.getCreditCard()!=null){
//                ProfileDetails.member.setCreditCard(member.getCreditCard());
//            }
//        }
//    }

    public static void resetMember() {
        ProfileDetails.setMember(null);
    }

    public static boolean isRewardsMember(){
        if(getMember()!=null){
            if(getMember().getRewardsNumber()!=null){
                return true;
            }
            else{
                return false;
            }
        }else{
            return false;
        }
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
}
