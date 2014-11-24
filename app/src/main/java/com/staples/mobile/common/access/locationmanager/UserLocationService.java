package com.staples.mobile.common.access.locationmanager;


import android.content.Context;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.List;
import java.util.Locale;


public class UserLocationService implements LatLngServiceCallBack{

    public static final String PREFS_NAME = "MyPrefsFile";
    Context context;
    boolean setDefaultLocation = false;
    ZipCodeCallBack zipCodeCallBack;
    LatLngCallBack latLngCallBack;
    LatLngService latlngService;

    public UserLocationService(Context context, LatLngCallBack latLngCallBack) {
        this.context = context;
        this.latLngCallBack = latLngCallBack;
    }

    public UserLocationService(Context context, ZipCodeCallBack zipCodeCallBack) {
        this.context = context;
        this.zipCodeCallBack = zipCodeCallBack;
    }


    public void getCurrentLocation(){
        this.latlngService = new LatLngService();
        this.latlngService.getUserCurrentLatLng();
    }

    public void getDefaultUserZipCode() {

        SharedPreferences sharedPref = this.context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        if (sharedPref.getString("userZipCode", null) == null) {
            this.setDefaultLocation = true;
            this.getCurrentLocation();
        } else {
            this.zipCodeCallBack.userZipCode(sharedPref.getString("userZipCode", null));
        }
    }

    public void getDefaultLatLng() {

        SharedPreferences sharedPref = this.context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        LatLng storedLatLng = this.getDefaultLatLng("userdefaultLatLng", LatLng.class);
        if (storedLatLng == null) {
            this.setDefaultLocation = true;
            this.getCurrentLocation();
        } else {
            this.latLngCallBack.userLatLng(storedLatLng);
        }
    }

    private void geoCodeLatLng(LatLng currentLatLan) {
        if (currentLatLan != null) {

            Geocoder geocoder = new Geocoder(this.context, Locale.getDefault());
            try {
                List<Address> addresses = geocoder.getFromLocation(currentLatLan.latitude, currentLatLan.longitude, 1);
                if (addresses != null) {
                    if (addresses.get(0).getPostalCode() != null) {
                        if (this.setDefaultLocation){
                            SharedPreferences sharedPref = this.context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPref.edit();
                            editor.putString("userZipCode", addresses.get(0).getPostalCode());
                            editor.commit();
                        }
                        this.zipCodeCallBack.userZipCode(addresses.get(0).getPostalCode());
                        return;
                    }
                }
            } catch (Exception e) {

            }
        }
        this.zipCodeCallBack.userZipCode(null);
    }

    //Credit:http://stackoverflow.com/questions/26541664/saving-latlng-and-listaddress-in-sharedpreference
    private <T> void setDefaultLatLng(String key, T value){
        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
        String data = gson.toJson(value);
        SharedPreferences.Editor ed = getSettings(context).edit();
        ed.putString(key, data);
        ed.commit();
    }


    private <T> T getDefaultLatLng(String key, Class<T> type){
        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
        String data = getSettings(this.context).getString(key, null);
        if (data==null){
            return null;
        }
        return gson.fromJson(data, type);
    }

    protected static SharedPreferences getSettings(Context context){
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return settings;
    }

    @Override
    public void latLng(LatLng latLng) {
        if (this.zipCodeCallBack != null) {
            if (this.setDefaultLocation) {
                this.setDefaultLatLng("userdefaultLatLng", latLng);
            }
            this.geoCodeLatLng(latLng);
        } else if (this.latLngCallBack != null) {
            this.latLngCallBack.userLatLng(latLng);
        } else {
            //TODO: throw error
        }
    }

}

interface LatLngServiceCallBack{
    void latLng(LatLng latLng);
}

class LatLngService implements GooglePlayServicesClient.ConnectionCallbacks, GooglePlayServicesClient.OnConnectionFailedListener, LocationListener {
    LocationRequest locationRequest;
    LocationClient locationClient;
    LatLngService latLngService;
    Context context;
    boolean locationEnabled;


    void LocationService(Context context, LatLngService latLngService){

        this.context = context;
        this.latLngService = latLngService;

        LocationManager manager = (LocationManager) this.context.getSystemService(Context.LOCATION_SERVICE);

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER) && !manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            locationEnabled = false;
        } else {
            locationEnabled = true;
        }

        locationClient = new LocationClient(this.context, this, this);
        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        locationRequest.setInterval(1);
        locationRequest.setFastestInterval(1);
    }


    public void getUserCurrentLatLng() {
    if (locationEnabled) {
        if (GooglePlayServicesUtil.isGooglePlayServicesAvailable(this.context) == ConnectionResult.SUCCESS) {

        } else {

        }
    } else {

    }

    }
    @Override
    public void onConnected(Bundle bundle) {
        Location location = locationClient.getLastLocation();
        if (location != null) {
            LatLng lstLng = new LatLng(location.getLatitude(),location.getLongitude());
        } else if (!locationEnabled) {
            //can't determine location
        } else {
            locationClient.requestLocationUpdates(locationRequest, (com.google.android.gms.location.LocationListener) this);
        }
    }

    @Override
    public void onDisconnected() {

    }

    @Override
    public void onLocationChanged(Location location) {
        //locationClient.removeLocationUpdates(null);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }
}