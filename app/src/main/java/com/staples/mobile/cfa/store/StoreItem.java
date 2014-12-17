package com.staples.mobile.cfa.store;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

class StoreItem {
    public String storeNumber;
    public LatLng position;
    public double distance;
    public String streetAddress1;
    public String streetAddress2;
    public String city;
    public String state;
    public String country;
    public String zipcode;
    public String phoneNumber;
    public String faxNumber;
    public Marker marker;
    private ArrayList<TimeSpan> spans;

    StoreItem(String storeNumber, double latitude, double longitude) {
        this.storeNumber = storeNumber;
        position = new LatLng(latitude, longitude);
        spans = new ArrayList<TimeSpan>(7);
    }

    public void addTimeSpan(TimeSpan span) {
        if (span!=null)
            spans.add(span);
    }

    public String formatHours(long when, DateFormat dateFormat) {
        long close;
        int until;

        // Loop for contiguous spans
        close = when;
        for(;;) {
            // Loop for span with longest open
            until = 0;
            for(TimeSpan span : spans) {
                int x = span.untilOutsideSpan(close);
                if (x>until) until = x;
            }

            // No span found
            if (until==0) break;

            // Extend close
            close += until;
        }

        // Close extended at least once
        if (close>when) {
            String clock = dateFormat.format(new Date(close));
            return ("Open until " + clock);
        }

        // Loop for span with shortest time until open
        until = TimeSpan.ONEWEEK;
        for(TimeSpan span : spans) {
            int x = span.untilInsideSpan(when);
            if (x<until) until = x;
        }

        // Open in less than one week
        if (until<TimeSpan.ONEWEEK) {
            String clock = dateFormat.format(new Date(when+until));
            return ("Open at " + clock);
        }

        return("Closed");
    }

    public static String reformatPhoneFaxNumber(String number) {
        if (number==null) return(null);
        number = number.trim();

        if (number.matches("[0-9]{10}")) {
            return("("+number.substring(0, 3)+") "+
                    number.substring(3, 6)+"-"+
                    number.substring(6, 10));
        }

        return(number);
    }
}
