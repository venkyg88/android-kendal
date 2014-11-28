package com.staples.mobile.cfa.location;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import java.util.List;

public class LocationFinder implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private static final String TAG = "LocationFinder";

    private static final String PREFS_PROVIDER = "locationProvider";
    private static final String PREFS_LATITUDE = "locationLatitude";
    private static final String PREFS_LONGITUDE = "locationLongitude";
    private static final String PREFS_TIMESTAMP = "locationTimestamp";
    private static final String PREFS_POSTALCODE = "locationPostalCode";

    private static LocationFinder instance;

    public static LocationFinder getInstance(Activity activity) {
        synchronized(LocationFinder.class) {
            if (instance == null) {
                instance = new LocationFinder(activity);
            }
            return (instance);
        }
    }

    private Activity activity;
    private GoogleApiClient client;
    private boolean connected;
    private Location location;
    private String postalCode;

    private LocationFinder(Activity activity) {
        this.activity = activity;
        loadRecentLocation();
        GoogleApiClient.Builder builder = new GoogleApiClient.Builder(activity, this, this);
        builder.addApi(LocationServices.API);
        client = builder.build();
        client.connect();
    }

    public Location getLocation() {
        Location latest = LocationServices.FusedLocationApi.getLastLocation(client);
        if (latest!=null) location = latest;
        return(location);
    }

    public String getPostalCode() {
        return(postalCode);
    }

    @Override
    public void onConnected (Bundle connectionHint) {
        Log.d(TAG, "GoogleApiClient connected");
        connected = true;

        Location latest =  LocationServices.FusedLocationApi.getLastLocation(client);
        if (latest!=null) location = latest;

        Geocoder geo = new Geocoder(activity);
        try {
            List<Address> addresses = geo.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            if (addresses!=null && addresses.size()>0) {
                Address address = addresses.get(0);
                postalCode = address.getPostalCode();
            }
        } catch (Exception e) {
            Log.d(TAG, "Error by Geocoder: "+e.toString());
        }
    }

    @Override
    public void onConnectionSuspended(int cause) {
        connected = false;
        Log.d(TAG, "GoogleApiClient suspended");
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        connected = false;
        Log.d(TAG, "GoogleApiClient connect failed: "+result.toString());
        if (result.hasResolution()) {
            try {
                Log.d(TAG, "Trying resolution");
                result.startResolutionForResult(activity, 0);
            } catch(Exception e) {};
        }
    }

    // Recent location

    public void loadRecentLocation() {
        SharedPreferences prefs = activity.getPreferences(Context.MODE_PRIVATE);
        if (prefs.contains(PREFS_LATITUDE) && prefs.contains(PREFS_LONGITUDE)) {
            location = new Location(prefs.getString(PREFS_PROVIDER, "Unknown"));
            location.setLatitude(prefs.getFloat(PREFS_LATITUDE, 0.0f));
            location.setLongitude(prefs.getFloat(PREFS_LONGITUDE, 0.0f));
            location.setTime(prefs.getLong(PREFS_TIMESTAMP, 0));
        }
        if (prefs.contains(PREFS_POSTALCODE))
            postalCode = prefs.getString(PREFS_POSTALCODE, null);
    }

    public void saveRecentLocation() {
        SharedPreferences prefs = activity.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        if (location!=null) {
            editor.putString(PREFS_PROVIDER, location.getProvider());
            editor.putFloat(PREFS_LATITUDE, (float) location.getLatitude());
            editor.putFloat(PREFS_LONGITUDE, (float) location.getLongitude());
            editor.putLong(PREFS_TIMESTAMP, location.getTime());
        }
        if (postalCode!=null)
            editor.putString(PREFS_POSTALCODE, postalCode);
        editor.apply();
    }
}
