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
                member.setCreditCard(ProfileDetails.member.getCreditCard());
                member.setAddress(ProfileDetails.member.getAddress());
                ProfileDetails.member = member;
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
}
