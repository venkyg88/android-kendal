package com.staples.mobile.cfa.location;

import android.content.Context;
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

    private static LocationFinder instance;

    public static LocationFinder getInstance(Context context) {
        synchronized(LocationFinder.class) {
            if (instance == null) {
                instance = new LocationFinder(context);
            }
            return (instance);
        }
    }

    private Context context;
    private GoogleApiClient client;
    private boolean connected;
    private String postalCode;

    private LocationFinder(Context context) {
        this.context = context;
        GoogleApiClient.Builder builder = new GoogleApiClient.Builder(context, this, this);
        builder.addApi(LocationServices.API);
        client = builder.build();
        client.connect();
    }

    public Location getLocation() {
        Location location = LocationServices.FusedLocationApi.getLastLocation(client);
        Log.d(TAG, "Last location: "+location);
        return(location);
    }

    public String getPostalCode() {
        Log.d(TAG, "Postal code: "+postalCode);
        return(postalCode);
    }

    @Override
    public void onConnected (Bundle connectionHint) {
        Log.d(TAG, "GoogleApiClient connected");
        connected = true;

        Location location =  LocationServices.FusedLocationApi.getLastLocation(client);
        Log.d(TAG, "Connected location: "+location);

        Geocoder geo = new Geocoder(context);
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
        Log.d(TAG, "GoogleApiClient connect failed");
    }
}
