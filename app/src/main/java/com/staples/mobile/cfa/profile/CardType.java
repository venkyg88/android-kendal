package com.staples.mobile.cfa.profile;

import com.staples.mobile.cfa.R;

import java.util.regex.Pattern;

/**
 * Created by Avinash Dodda.
 */

public enum CardType {
    // lower case card type names are required for adding cards to cart or profile, upper case required for POW service
    UNKNOWN         (null,                            null,         0),
    AMERICAN_EXPRESS("^3[47][0-9]{13}$",              "AMEX",       R.drawable.american_express),
    VISA            ("^4[0-9]{12}(?:[0-9]{3})?$",     "Visa",       R.drawable.visa),
    MASTERCARD      ("^5[1-5][0-9]{14}$",             "Mastercard", R.drawable.mastercard),
    DISCOVER        ("^6(?:011|5[0-9]{2})[0-9]{12}$", "Discover",   R.drawable.discover),
    STAPLES         ("^7972[0-9]{12}$",               "Staples",    R.drawable.ic_launcher);

    private Pattern pattern;
    private String cardTypeName;
    private int imageResource;

    CardType(String pattern, String cardTypeName, int imageResource) {
        this.pattern = pattern == null? null : Pattern.compile(pattern);
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

    public static CardType matchOnApiName(String apiCardTypeName) {
        if (apiCardTypeName != null) {
            String upperCaseName = apiCardTypeName.toUpperCase();
            if( upperCaseName.equals("VI") || upperCaseName.equals("VISA")) {
                return VISA;
            } else if(upperCaseName.equals("AM")  || upperCaseName.equals("AMEX")) {
                return AMERICAN_EXPRESS;
            } else if(upperCaseName.equals("MC") || upperCaseName.equals("MASTERCARD")) {
                return MASTERCARD;
            } else if(upperCaseName.equals("DI") || upperCaseName.equals("DISC") || upperCaseName.equals("DISCOVER")) {
                return DISCOVER;
            }
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