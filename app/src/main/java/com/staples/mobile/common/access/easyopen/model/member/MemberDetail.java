package com.staples.mobile.common.access.easyopen.model.member;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.staples.mobile.common.access.easyopen.model.ApiError;
import com.staples.mobile.common.access.easyopen.model.SupportsApiErrors;

import java.util.List;

/**
 * Created by Avinash Dodda.
 */
public class MemberDetail implements SupportsApiErrors {

    @JsonProperty("Member")
    private List<Member> member;

    // include this so that ((MemberDetail)retrofitError.getBody()).getErrors()
    // can be examined in 400 Bad Request failure response (e.g. in debugger)
    private List<ApiError> errors;
    public List<ApiError> getErrors() { return errors; }
    public void setErrors(List<ApiError> errors) { this.errors = errors; }

    public List<Member> getMember() {
        return member;
    }

    public void setMember(List<Member> member) {
        this.member = member;
    }
}
