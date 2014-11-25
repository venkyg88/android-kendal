package com.staples.mobile.cfa.location.LatLngService;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.maps.model.LatLng;

/**
 * Created by parve002 on 11/24/14.
 */
public class LatLngService implements GooglePlayServicesClient.ConnectionCallbacks, GooglePlayServicesClient.OnConnectionFailedListener {

    Context context;
    LocationClient locationClient;
    Location currentLocation;
    public LatLngServiceCallBack latLngServiceCallBack;


    public LatLngService(Context context, LatLngServiceCallBack latLngServiceCallBack){

        this.latLngServiceCallBack = latLngServiceCallBack;
        this.context = context;
        this.locationClient = new LocationClient(context, this, this);

        if (locationClient.isConnected()){
            Location location = this.locationClient.getLastLocation();
            this.locationReceived(location);
        } else {
            locationClient.connect();
        }
    }


    private void locationReceived(Location location) {

        if (location != null) {
            if (this.latLngServiceCallBack == null) {
                //TODO: Throw error - latLngServiceCallBack is null
            } else {
                this.currentLocation = location;
                LatLng newLatLng = new LatLng(this.currentLocation.getLatitude(), this.currentLocation.getLongitude());
                this.latLngServiceCallBack.onLatLngServiceCallBack(newLatLng);
            }
        }
    }


    public void onConnected(Bundle bundle) {

        Location location = this.locationClient.getLastLocation();

        if (location != null) {
            this.locationReceived(location);
            locationClient.disconnect();
        }
    }

    public void onDisconnected() {
        Log.i("onDisconnected","");
    }

    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i("Error connecting","");
    }

}
