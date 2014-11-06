package com.staples.mobile.common.access.easyopen.model.login;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.staples.mobile.common.access.easyopen.model.ApiError;
import com.staples.mobile.common.access.easyopen.model.SupportsApiErrors;

import java.util.List;

/**
 * Created by Avinash Dodda
 */
public class TokenObject implements SupportsApiErrors {
    @JsonProperty("WCToken")
    private String WCToken;
    @JsonProperty("WCTrustedToken")
    private String WCTrustedToken;
    private String personalizationID;
    private String userId;

    // include this so that ((SupportsApiErrors)retrofitError.getBody()).getErrors()
    // can be examined in 400 Bad Request failure response (e.g. in debugger)
    private List<ApiError> errors;
    public List<ApiError> getErrors() { return errors; }
    public void setErrors(List<ApiError> errors) { this.errors = errors; }


    public String getWCToken() {
        return WCToken;
    }

    public void setWCToken(String WCToken) {
        this.WCToken = WCToken;
    }

    public String getWCTrustedToken() {
        return WCTrustedToken;
    }

    public void setWCTrustedToken(String WCTrustedToken) {
        this.WCTrustedToken = WCTrustedToken;
    }

    public String getPersonalizationID() {
        return personalizationID;
    }

    public void setPersonalizationID(String personalizationID) {
        this.personalizationID = personalizationID;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
