package com.staples.mobile.common.access.easyopen.model.member;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.staples.mobile.common.access.easyopen.model.BaseResponse;

import java.util.List;

/**
 * Created by Avinash Dodda.
 */
public class MemberDetail extends BaseResponse {

    @JsonProperty("Member")
    private List<Member> member;

    public List<Member> getMember() {
        return member;
    }

    public void setMember(List<Member> member) {
        this.member = member;
    }
}
