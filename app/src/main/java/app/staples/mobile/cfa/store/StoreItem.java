package app.staples.mobile.cfa.store;

import com.google.android.gms.maps.model.Marker;

import java.util.ArrayList;

public class StoreItem {
    public String storeNumber;
    public double latitude;
    public double longitude;
    public double distance;
    public String streetAddress1;
    public String streetAddress2;
    public String city;
    public String state;
    public String country;
    public String postalCode;
    public String phoneNumber;
    public String faxNumber;
    public String storeFeatures;
    public Marker marker;
    private ArrayList<TimeSpan> spans;

    public StoreItem() {
        spans = new ArrayList<TimeSpan>(7);
    }

    public void addTimeSpan(TimeSpan span) {
        if (span!=null)
            spans.add(span);
    }

    public ArrayList<TimeSpan> getSpans() {
        return(spans);
    }

    public String formatCityState() {
        if (city==null) {
            if (state==null) return(null);
            else return(state);
        } else {
            if (state==null) return(city);
            else return(city+", "+state);
        }
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
