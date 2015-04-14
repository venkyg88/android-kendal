package com.staples.mobile.cfa.profile;

import com.staples.mobile.cfa.R;

import java.util.regex.Pattern;

public class CreditCard {
    public enum Type {
        // lower case card type names are required for adding cards to cart or profile, upper case required for POW service
        UNKNOWN(null, null, R.drawable.unknowncc, true),
        AMERICAN_EXPRESS("^3[47][0-9]{13}$", "AMEX", R.drawable.american_express, true),
        VISA("^4[0-9]{12}(?:[0-9]{3})?$", "Visa", R.drawable.visa, true),
        MASTERCARD("^5[1-5][0-9]{14}$", "Mastercard", R.drawable.mastercard, true),
        DISCOVER("^6(?:011|5[0-9]{2})[0-9]{12}$", "Discover", R.drawable.discover, true),
        STAPLES("^7972[0-9]{12}$", "Staples", R.drawable.ic_staples_card, false);

        private Pattern pattern;
        private String name;
        private int imageResource;
        private boolean cidUsed;

        Type(String pattern, String cardTypeName, int imageResource, boolean cidUsed) {
            this.pattern = pattern == null ? null : Pattern.compile(pattern);
            this.name = cardTypeName;
            this.imageResource = imageResource;
            this.cidUsed = cidUsed;
        }

        public String getName() {
            return name;
        }

        public int getImageResource() {
            return imageResource;
        }

        public boolean isCidUsed() { return cidUsed; }

        public static Type detect(String cardNumber) {

            for(Type type : Type.values()) {
                if (null == type.pattern) continue;
                if (type.pattern.matcher(cardNumber).matches()) return type;
            }
            return UNKNOWN;
        }

        public static Type matchOnApiName(String apiCardTypeName) {
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
    }

    private Type type;
    private String number;

    public CreditCard(Type type, String number) {
        this.type = type;
        this.number = number;
    }

    public Type getType() {
        return(type);
    }

    public String getNumber() {
        return(number);
    }

    public boolean isChecksumValid() {
        // Safety check
        if (number==null) return(false);
        int n = number.length();
        if (n<12) return(false);

        // Use Luhn algorithm
        int sum = 0;
        for(int i=0;i<n;i++) {
            char c = number.charAt(i);
            if (c<'0' || c>'9') return(false);
            if (((n-i)&1)==0) {
                sum += 2*(c-'0');
                if (c>='5') sum++;
            } else sum += (c-'0');
        }
        return((sum%10)==0);
    }
}
