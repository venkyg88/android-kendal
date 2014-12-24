package com.staples.mobile.cfa;

import java.util.regex.Pattern;

public enum IdentifierType {
    TOPCATEGORY   ("-[0-9]+"),
    SUPERCATEGORY ("SC[0-9]+"),
    CATEGORY      ("CG[0-9]+"),
    DEPARTMENT    ("DP[0-9]+"),
    CLASS         ("CL[0-9]+"),
    BUNDLE        ("BI[0-9]+"),
    SKUSET        ("SS[0-9]+"),
    SKU           ("[0-9]+"),
    EMPTY         (""),
    UNKNOWN       (null);

    private Pattern pattern;

    private IdentifierType(String pattern) {
        if (pattern!=null)
            this.pattern = Pattern.compile(pattern);
    }

    public static IdentifierType detect(String string) {
        if (string==null) return(EMPTY);
        for(IdentifierType type : IdentifierType.values()) {
            if (type.pattern!=null &&
                type.pattern.matcher(string).matches())
                return(type);
        }
        return(UNKNOWN);
    }
}
