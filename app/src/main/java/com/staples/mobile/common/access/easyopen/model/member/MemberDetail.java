package com.staples.mobile.common.access.easyopen.model.member;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Created by Avinash Dodda.
 */
public class MemberDetail {

    @JsonProperty("Member")
    private List<Member> member;

    public List<Member> getMember() {
        return member;
    }

    public void setMember(List<Member> member) {
        this.member = member;
    }
}
