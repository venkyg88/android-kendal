package com.staples.mobile.common.access.login.model;

/**
 * Created by Avinash Dodda
 */

public class RegisteredUserLogin {

    private String logonId;

    private String logonPassword;

    public RegisteredUserLogin(String logonId, String logonPassword) {
        this.logonId = logonId;
        this.logonPassword = logonPassword;
    }

    public String getLogonId() {
        return logonId;
    }

    public void setLogonId(String logonId) {
        this.logonId = logonId;
    }

    public String getLogonPassword() {
        return logonPassword;
    }

    public void setLogonPassword(String logonPassword) {
        this.logonPassword = logonPassword;
    }
}
