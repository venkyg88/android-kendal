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
}
