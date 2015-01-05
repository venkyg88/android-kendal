package com.staples.mobile.cfa.profile;

import android.util.Log;

import java.util.Arrays;

public enum UsState {
    // This must be sorted strictly by state name
    AL ("AL", "Alabama"),
    AK ("AK", "Alaska"),
    AZ ("AZ", "Arizona"),
    AR ("AR", "Arkansas"),
    CA ("CA", "California"),
    CO ("CO", "Colorado"),
    CT ("CT", "Connecticut"),
    DE ("DE", "Delaware"),
    FL ("FL", "Florida"),
    GA ("GA", "Georgia"),
    HI ("HI", "Hawaii"),
    ID ("ID", "Idaho"),
    IL ("IL", "Illinois"),
    IN ("IN", "Indiana"),
    IA ("IA", "Iowa"),
    KS ("KS", "Kansas"),
    KY ("KY", "Kentucky"),
    LA ("LA", "Louisiana"),
    ME ("ME", "Maine"),
    MD ("MD", "Maryland"),
    MA ("MA", "Massachusetts"),
    MI ("MI", "Michigan"),
    MN ("MN", "Minnesota"),
    MS ("MS", "Mississippi"),
    MO ("MO", "Missouri"),
    MT ("MT", "Montana"),
    NE ("NE", "Nebraska"),
    NV ("NV", "Nevada"),
    NH ("NH", "New Hampshire"),
    NJ ("NJ", "New Jersey"),
    NM ("NM", "New Mexico"),
    NY ("NY", "New York"),
    NC ("NC", "North Carolina"),
    ND ("ND", "North Dakota"),
    OH ("OH", "Ohio"),
    OK ("OK", "Oklahoma"),
    OR ("OR", "Oregon"),
    PA ("PA", "Pennsylvania"),
    RI ("RI", "Rhode Island"),
    SC ("SC", "South Carolina"),
    SD ("SD", "South Dakota"),
    TN ("TN", "Tennessee"),
    TX ("TX", "Texas"),
    UT ("UT", "Utah"),
    VT ("VT", "Vermont"),
    VA ("VA", "Virginia[G]"),
    WA ("WA", "Washington"),
    DC ("DC", "Washington DC"),
    WV ("WV", "West Virginia"),
    WI ("WI", "Wisconsin"),
    WY ("WY", "Wyoming");

    public String abbr;
    public String name;

    UsState(String abbr, String name) {
        this.abbr = abbr;
        this.name = name;
    }

    public String toString() {
        return(name);
    }

    public static UsState findByAbbr(String text) {
        if (text==null) return(null);
        text = text.trim();

        // Binary search
        UsState[] values = UsState.values();
        int a = 0;
        int b = values.length-1;
        for(;a<=b;) {
            int i = (a+b)/2;
            UsState state = values[i];
            int s = state.abbr.compareToIgnoreCase(text);
            if (s==0) return(state);
            else if (s<0) a = i+1;
            else b = i-1;
        }

        // Darn, check linearly
        for(UsState state : values) {
            if (state.abbr.compareToIgnoreCase(text)==0)
                return(state);
        }
        return(null);
    }

    public static UsState findByName(String text) {
        if (text==null) return(null);
        text = text.trim();

        // Binary search
        UsState[] values = UsState.values();
        int a = 0;
        int b = values.length-1;
        for(;a<=b;) {
            int i = (a+b)/2;
            UsState state = values[i];
            int s = values[i].name.compareToIgnoreCase(text);
            if (s==0) return(state);
            else if (s<0) a = i+1;
            else b = i-1;
        }
        return(null);
    }
}
