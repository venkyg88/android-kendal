package com.staples.mobile.common.access.easyopen.model.login;

/**
 * Created by Avinash Dodda.
 */
public class CreateUserLogin {

    private String email1;
    private String newReEnterEmailAddr;
    private String logonId;
    private String logonPassword;
    private String logonPasswordVerify;
    private String challengeQuestion;
    private String challengeAnswer;
    private String autoLoginPreference;
    private String emailPreference;

    public CreateUserLogin(String email1, String logonId, String logonPassword) {
        this.email1 = email1;
        this.newReEnterEmailAddr = email1;
        this.logonId = logonId;
        this.logonPassword = logonPassword;
        this.logonPasswordVerify = logonPassword;
        this.challengeQuestion = "Where do you work?";
        this.challengeAnswer = "Boston";
        this.autoLoginPreference = "1";
        this.emailPreference = "emailNo";
    }
}
