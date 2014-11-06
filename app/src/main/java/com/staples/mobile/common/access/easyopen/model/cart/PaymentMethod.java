/*
 * Copyright (c) 2014 Staples, Inc. All rights reserved.
 */

package com.staples.mobile.common.access.easyopen.model.cart;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.staples.mobile.common.access.easyopen.model.member.CCDetails;

/**
 * Created by Diana Sutlief.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PaymentMethod {

    private String creditCardId;
    private String cardType;
    private String cardNumber; // this is the encrypted value from the call to the Credit Card Encryption Service
    private String cardExpirationMonth;
    private String cardExpirationYear;
    private String cardVerificationCode; // must be entered each time by user
    private String saveCard;
    private String notes;

    // default constructor
    public PaymentMethod() { }


    // conversion constructor
    public PaymentMethod(CCDetails profileCc) {
        this.creditCardId = profileCc.getCreditCardId();
    }

    public String getCreditCardId() {
        return creditCardId;
    }

    public void setCreditCardId(String creditCardId) {
        this.creditCardId = creditCardId;
    }

    public String getCardType() {
        return cardType;
    }

    public void setCardType(String cardType) {
        this.cardType = cardType;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getCardExpirationMonth() {
        return cardExpirationMonth;
    }

    public void setCardExpirationMonth(String cardExpirationMonth) {
        this.cardExpirationMonth = cardExpirationMonth;
    }

    public String getCardExpirationYear() {
        return cardExpirationYear;
    }

    public void setCardExpirationYear(String cardExpirationYear) {
        this.cardExpirationYear = cardExpirationYear;
    }

    public String getCardVerificationCode() {
        return cardVerificationCode;
    }

    public void setCardVerificationCode(String cardVerificationCode) {
        this.cardVerificationCode = cardVerificationCode;
    }

    public String getSaveCard() {
        return saveCard;
    }

    public void setSaveCard(String saveCard) {
        this.saveCard = saveCard;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
