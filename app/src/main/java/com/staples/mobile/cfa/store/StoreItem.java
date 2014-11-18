package com.staples.mobile.cfa.store;

import com.google.android.gms.maps.model.LatLng;

class StoreItem {
    public LatLng position;
    public String storeNumber;
    public String streetAddress1;
    public String streetAddress2;
    public String city;
    public String state;
    public String country;
    public String zipcode;
    public String phoneNumber;

    StoreItem(String storeNumber, double latitude, double longitude) {
        this.storeNumber = storeNumber;
        position = new LatLng(latitude, longitude);
    }
}
