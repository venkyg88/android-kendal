package com.staples.mobile.cfa.profile;

import android.util.Log;

import com.staples.mobile.cfa.R;

import java.util.regex.Pattern;

/**
 * Created by Avinash Dodda.
 */

public enum CardType {
    // lower case card type names are required for adding cards to cart or profile, upper case required for POW service
    UNKNOWN         (null,                            null,         R.drawable.unknowncc),
    AMERICAN_EXPRESS("^3[47][0-9]{13}$",              "AMEX",       R.drawable.american_express),
    VISA            ("^4[0-9]{12}(?:[0-9]{3})?$",     "Visa",       R.drawable.visa),
    MASTERCARD      ("^5[1-5][0-9]{14}$",             "Mastercard", R.drawable.mastercard),
    DISCOVER        ("^6(?:011|5[0-9]{2})[0-9]{12}$", "Discover",   R.drawable.discover),
    STAPLES         ("^7972[0-9]{12}$",               "Staples",    R.drawable.ic_android);

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

    public static boolean isChecksumValid(String cardNumber) {
        // Safety check
        if (cardNumber==null) return(false);
        cardNumber = cardNumber.trim();
        int n = cardNumber.length();
        if (n<12) return(false);

        // Use Luhn algorithm
        int sum = 0;
        for(int i=0;i<n;i++) {
            char c = cardNumber.charAt(i);
            if (c<'0' || c>'9') return(false);
            if (((n-i)&1)==0) {
                sum += 2*(c-'0');
                if (c>='5') sum++;
            } else sum += (c-'0');
        }
        return((sum%10)==0);
    }

    public String getCardTypeName() {
        return cardTypeName;
    }

    public int getImageResource() {
        return imageResource;
    }
}