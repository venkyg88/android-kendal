package com.staples.mobile.common.access.easyopen.model.login;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.staples.mobile.common.access.easyopen.model.BaseResponse;


/**
 * Created by Avinash Dodda
 */
public class TokenObject extends BaseResponse {
    @JsonProperty("WCToken")
    private String WCToken;
    @JsonProperty("WCTrustedToken")
    private String WCTrustedToken;
    private String personalizationID;
    private String userId;

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
