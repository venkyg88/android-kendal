package com.staples.mobile.common.access.easyopen.model.member;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Created by Avinash Dodda
 */
public class Member {

    private String autoLoginFlag;
    private int creditCardCount;
    private String emailAddress;
    private String isUserSubscribed;
    private String openAccountEnabledFlag;
    private String password;
    private String reminderQuestion;
    private String rewardsNumber;
    private String rewardsNumberVerified;
    private int storedAddressCount;
    private String userName;
    private String welcomeMessage;
    private Reward[] rewardDetails;
    private FavouriteListDetail[] favoritesList;
    private List<PreferredStore> preferredStore;
    private List<CCDetails> creditCard;
    private List<Address> address;

    public List<CCDetails> getCreditCard() {
        return creditCard;
    }

    public void setCreditCard(List<CCDetails> creditCard) {
        this.creditCard = creditCard;
    }

    public List<Address> getAddress() {
        return address;
    }

    public void setAddress(List<Address> address) {
        this.address = address;
    }

    public List<PreferredStore> getPreferredStore() {
        return preferredStore;
    }

    public void setPreferredStore(List<PreferredStore> preferredStore) {
        this.preferredStore = preferredStore;
    }

    public String getAutoLoginFlag() {
        return autoLoginFlag;
    }

    public void setAutoLoginFlag(String autoLoginFlag) {
        this.autoLoginFlag = autoLoginFlag;
    }

    public int getCreditCardCount() {
        return creditCardCount;
    }

    public void setCreditCardCount(int creditCardCount) {
        this.creditCardCount = creditCardCount;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getIsUserSubscribed() {
        return isUserSubscribed;
    }

    public void setIsUserSubscribed(String isUserSubscribed) {
        this.isUserSubscribed = isUserSubscribed;
    }

    public String getOpenAccountEnabledFlag() {
        return openAccountEnabledFlag;
    }

    public void setOpenAccountEnabledFlag(String openAccountEnabledFlag) {
        this.openAccountEnabledFlag = openAccountEnabledFlag;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getReminderQuestion() {
        return reminderQuestion;
    }

    public void setReminderQuestion(String reminderQuestion) {
        this.reminderQuestion = reminderQuestion;
    }

    public String getRewardsNumber() {
        return rewardsNumber;
    }

    public void setRewardsNumber(String rewardsNumber) {
        this.rewardsNumber = rewardsNumber;
    }

    public String getRewardsNumberVerified() {
        return rewardsNumberVerified;
    }

    public void setRewardsNumberVerified(String rewardsNumberVerified) {
        this.rewardsNumberVerified = rewardsNumberVerified;
    }

    public int getStoredAddressCount() {
        return storedAddressCount;
    }

    public void setStoredAddressCount(int storedAddressCount) {
        this.storedAddressCount = storedAddressCount;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getWelcomeMessage() {
        return welcomeMessage;
    }

    public void setWelcomeMessage(String welcomeMessage) {
        this.welcomeMessage = welcomeMessage;
    }

    public Reward[] getRewardDetails() {
        return rewardDetails;
    }

    public void setRewardDetails(Reward[] rewardDetails) {
        this.rewardDetails = rewardDetails;
    }

    public FavouriteListDetail[] getFavoritesList() {
        return favoritesList;
    }

    public void setFavoritesList(FavouriteListDetail[] favoritesList) {
        this.favoritesList = favoritesList;
    }
}
