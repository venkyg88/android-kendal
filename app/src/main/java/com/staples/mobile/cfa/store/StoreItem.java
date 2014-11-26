package com.staples.mobile.cfa.store;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

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

    public String formatHours(long time, int dateStyle, DateFormat timeFormat) {
        int until;
        TimeSpan best;

        // Check until closed
        until = 0;
        best = null;
        for(TimeSpan span : spans) {
            int x = span.untilOutsideSpan(time);
            if (x>until) {
                until = x;
                best = span;
            }
        }
        if (best!=null) {
            Calendar calendar = timeFormat.getCalendar();
            calendar.setTimeInMillis(best.getStart());
            String dow = calendar.getDisplayName(Calendar.DAY_OF_WEEK, dateStyle, Locale.getDefault());
            String close = timeFormat.format(best.getEnd());
            return ("Open until\n" + dow + " " + close);
        }

        // Check until open
        until = TimeSpan.ONEWEEK;
        best = null;
        for(TimeSpan span : spans) {
            int x = span.untilInsideSpan(time);
            if (x<until) {
                until = x;
                best = span;
            }
        }
        if (best!=null) {
            Calendar calendar = timeFormat.getCalendar();
            calendar.setTimeInMillis(best.getStart());
            String dow = calendar.getDisplayName(Calendar.DAY_OF_WEEK, dateStyle, Locale.getDefault());
            String open = timeFormat.format(best.getStart());
            return ("Open at\n" + dow + " " + open);

        }

        return("Closed");
    }
}
