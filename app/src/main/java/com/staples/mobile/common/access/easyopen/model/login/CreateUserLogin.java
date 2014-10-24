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

    public String getEmail1() {
        return email1;
    }

    public void setEmail1(String email1) {
        this.email1 = email1;
    }

    public String getNewReEnterEmailAddr() {
        return newReEnterEmailAddr;
    }

    public void setNewReEnterEmailAddr(String newReEnterEmailAddr) {
        this.newReEnterEmailAddr = newReEnterEmailAddr;
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

    public String getLogonPasswordVerify() {
        return logonPasswordVerify;
    }

    public void setLogonPasswordVerify(String logonPasswordVerify) {
        this.logonPasswordVerify = logonPasswordVerify;
    }

    public String getChallengeQuestion() {
        return challengeQuestion;
    }

    public void setChallengeQuestion(String challengeQuestion) {
        this.challengeQuestion = challengeQuestion;
    }

    public String getChallengeAnswer() {
        return challengeAnswer;
    }

    public void setChallengeAnswer(String challengeAnswer) {
        this.challengeAnswer = challengeAnswer;
    }

    public String getAutoLoginPreference() {
        return autoLoginPreference;
    }

    public void setAutoLoginPreference(String autoLoginPreference) {
        this.autoLoginPreference = autoLoginPreference;
    }

    public String getEmailPreference() {
        return emailPreference;
    }

    public void setEmailPreference(String emailPreference) {
        this.emailPreference = emailPreference;
    }

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
