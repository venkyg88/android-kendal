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
    public String storeFeatures;
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

    public ArrayList<TimeSpan> getSpans() {
        return(spans);
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
