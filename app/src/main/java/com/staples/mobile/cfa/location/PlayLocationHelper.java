package com.staples.mobile.cfa.location;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.maps.model.LatLng;
import com.staples.mobile.cfa.location.LocationService;

/**
 * Created by parve002 on 11/24/14.
 */
public class PlayLocationHelper implements GooglePlayServicesClient.ConnectionCallbacks, GooglePlayServicesClient.OnConnectionFailedListener {

    Context context;
    LocationClient locationClient;
    Location currentLocation;
    public LocationService userLocationService;

    /**
     * This constructor initiates fetching the location.
     * @param context is the Context of the caller.
     * @param userLocationService is the reference to the caller.
     */
    public PlayLocationHelper(Context context, LocationService userLocationService){

        this.userLocationService = userLocationService;
        this.context = context;
        this.locationClient = new LocationClient(context, this, this);

        if (locationClient.isConnected()){
            Location location = this.locationClient.getLastLocation();
            this.locationReceived(location);
        } else {
            locationClient.connect();
        }
    }

    /**
     * This function will be the only one to execute the call back. The Locations received by any mechanism must call this function,
     * @param location is the current location of the user.
     */
    private void locationReceived(Location location) {

        if (location != null) {
            if (this.userLocationService == null) {
                //TODO: Throw error - latLngServiceCallBack is null
            } else {
                this.currentLocation = location;
                LatLng newLatLng = new LatLng(this.currentLocation.getLatitude(), this.currentLocation.getLongitude());
                this.userLocationService.onLatLngServiceReceived(newLatLng);
            }
        }
    }


// GooglePlayServicesClient Call backs

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
