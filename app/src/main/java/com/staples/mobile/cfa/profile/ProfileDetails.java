package com.staples.mobile.cfa.profile;

import com.staples.mobile.common.access.easyopen.model.member.Member;

/**
 * Created by Avinash Dodda.
 */
public class ProfileDetails {

    private static Member member;

    public static Member getMember() {
        return member;
    }

    public static void setMember(Member member) {
        ProfileDetails.member = member;
    }

    public static void mergeMember(Member member) {

        if(ProfileDetails.member == null){
            ProfileDetails.member = member;
        } else {
            if(member.getUserName()!=null){
                ProfileDetails.member.setUserName(member.getUserName());
                ProfileDetails.member.setEmailAddress(member.getEmailAddress());
                ProfileDetails.member.setAutoLoginFlag(member.getAutoLoginFlag());
                ProfileDetails.member.setCreditCardCount(member.getCreditCardCount());
                ProfileDetails.member.setIsUserSubscribed(member.getIsUserSubscribed());
                ProfileDetails.member.setOpenAccountEnabledFlag(member.getOpenAccountEnabledFlag());
                ProfileDetails.member.setReminderQuestion(member.getReminderQuestion());
                ProfileDetails.member.setRewardsNumberVerified(member.getRewardsNumberVerified());
                ProfileDetails.member.setRewardsNumber(member.getRewardsNumber());
                ProfileDetails.member.setStoredAddressCount(member.getStoredAddressCount());
                ProfileDetails.member.setWelcomeMessage(member.getWelcomeMessage());
                ProfileDetails.member.setRewardDetails(member.getRewardDetails());
                ProfileDetails.member.setFavoritesList(member.getFavoritesList());
//                ProfileDetails.member.setCreditCard(member.getCreditCard());
                ProfileDetails.member.setPreferredStore(member.getPreferredStore());
//                ProfileDetails.member.setAddress(member.getAddress());
            }

            if(member.getAddress()!=null){
                ProfileDetails.member.setAddress(member.getAddress());
            }

            if(member.getCreditCard()!=null){
                ProfileDetails.member.setCreditCard(member.getCreditCard());
            }
        }
    }

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
    public static boolean hasAddressOrPaymentMethod() {
        if (getMember() != null) {
            if (getMember().getCreditCardCount() > 0 || getMember().getStoredAddressCount() > 0) {
                return true;
            }
        }
        return false;
    }
}
