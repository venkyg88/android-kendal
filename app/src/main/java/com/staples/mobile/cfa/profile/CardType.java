package com.staples.mobile.cfa.profile;

import java.util.regex.Pattern;

/**
 * Created by Avinash Dodda.
 */

public enum CardType {

    UNKNOWN,
    VISA("^4[0-9]{12}(?:[0-9]{3})?$"),
    MASTERCARD("^5[1-5][0-9]{14}$"),
    AMERICAN_EXPRESS("^3[47][0-9]{13}$"),
    DISCOVER("^6(?:011|5[0-9]{2})[0-9]{12}$");

    private Pattern pattern;

    CardType() {
        this.pattern = null;
    }

    CardType(String pattern) {
        this.pattern = Pattern.compile(pattern);
    }

    public static CardType detect(String cardNumber) {

        for (CardType cardType : CardType.values()) {
            if (null == cardType.pattern) continue;
            if (cardType.pattern.matcher(cardNumber).matches()) return cardType;
        }
        return UNKNOWN;
    }

}