package com.staples.mobile.cfa.profile;

import com.staples.mobile.common.access.easyopen.model.member.Member;

/**
 * Created by Avinash Dodda.
 */
public class MemberObject {

    private static Member member;

    public static Member getMember() {
        return member;
    }

    public static void setMember(Member member) {
        MemberObject.member = member;
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
