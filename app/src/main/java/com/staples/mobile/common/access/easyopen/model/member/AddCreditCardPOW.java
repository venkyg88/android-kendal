package com.staples.mobile.common.access.easyopen.model.member;

/**
 * Created by Avinash Dodda.
 */
public class AddCreditCardPOW {

    private String ccn;
    private String ccType;

    public String getCcn() {
        return ccn;
    }

    public void setCcn(String ccn) {
        this.ccn = ccn;
    }

    public String getCcType() {
        return ccType;
    }

    public void setCcType(String ccType) {
        this.ccType = ccType;
    }

    public AddCreditCardPOW(String ccn, String ccType) {

        this.ccn = ccn;
        this.ccType = ccType;
    }
}
