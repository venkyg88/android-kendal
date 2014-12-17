package com.staples.mobile.cfa.profile;

import com.staples.mobile.cfa.R;

import java.util.regex.Pattern;

/**
 * Created by Avinash Dodda.
 */

public enum CardType {

    UNKNOWN,
    VISA("^4[0-9]{12}(?:[0-9]{3})?$", "VISA", R.drawable.visa),
    MASTERCARD("^5[1-5][0-9]{14}$", "MASTERCARD", R.drawable.mastercard),
    AMERICAN_EXPRESS("^3[47][0-9]{13}$", "AMEX", R.drawable.american_express),
    DISCOVER("^6(?:011|5[0-9]{2})[0-9]{12}$", "DISCOVER", R.drawable.discover);

    private Pattern pattern;
    private String cardTypeName;
    private int imageResource;

    CardType() {
        this.pattern = null;
    }

    CardType(String pattern, String cardTypeName, int imageResource) {
        this.pattern = Pattern.compile(pattern);
        this.cardTypeName = cardTypeName;
        this.imageResource = imageResource;
    }

    public static CardType detect(String cardNumber) {

        for (CardType cardType : CardType.values()) {
            if (null == cardType.pattern) continue;
            if (cardType.pattern.matcher(cardNumber).matches()) return cardType;
        }
        return UNKNOWN;
    }

    public String getCardTypeName() {
        return cardTypeName;
    }

    public int getImageResource() {
        return imageResource;
    }
}