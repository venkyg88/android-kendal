package app.staples.mobile.cfa.location;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.crittercism.app.Crittercism;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import app.staples.mobile.cfa.MainActivity;
import com.staples.mobile.common.analytics.Tracker;

import java.util.List;

public class LocationFinder implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private static final String TAG = LocationFinder.class.getSimpleName();

    private static final String PREFS_PROVIDER = "locationProvider";
    private static final String PREFS_LATITUDE = "locationLatitude";
    private static final String PREFS_LONGITUDE = "locationLongitude";
    private static final String PREFS_TIMESTAMP = "locationTimestamp";
    private static final String PREFS_POSTALCODE = "postalCode";

    // default Framingham store info from
    // http://sapi.staples.com/v1/10001/stores/search?zipCode=01701&locale=en_US&catalogId=10051&distance=50&client_id=N6CA89Ti14E6PAbGTr5xsCJ2IGaHzGwS
    private static final double DefaultLatitude = 42.2986;
    private static final double DefaultLongitude = -71.423;

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
    private final Geocoder geocoder;

    private boolean connected;
    private Location location;
    private String postalCode;
    private long startTime;

    public interface PostalCodeCallback {
        void onGetPostalCodeSuccess(String postalCode);
        void onGetPostalCodeFailure();
    }

    private LocationFinder(Context context) {
        this.context = context;
        loadRecentLocation();

        geocoder = new Geocoder(context);

        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(context);
        switch(status) {
            case ConnectionResult.SUCCESS:
                GoogleApiClient.Builder builder = new GoogleApiClient.Builder(context);
                builder.addApi(LocationServices.API);
                builder.addConnectionCallbacks(this);
                builder.addOnConnectionFailedListener(this);
                client = builder.build();
                startTime = System.currentTimeMillis();
                client.connect();
                break;
            default:
                String msg = "Google Play Services: "+GooglePlayServicesUtil.getErrorString(status);
                Log.d(TAG, msg);
                break;
        }
    }

    private class Resolver extends Thread {

        private PostalCodeCallback callback;
        public Resolver(@Nullable PostalCodeCallback callback) {
            this.callback = callback;
        }

        public Resolver() {}

        @Override
        public void run() {
            if (location == null) return;

            try {
                startTime = System.currentTimeMillis();
                List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                if (addresses != null && addresses.size() > 0) {
                    String msg = "Geocoder completed in "+Long.toString(System.currentTimeMillis()-startTime)+"ms";
                    Log.d(TAG, msg);
                    Address address = addresses.get(0);
                    postalCode = address.getPostalCode();
                    if (callback != null) {
                        this.callback.onGetPostalCodeSuccess(postalCode);
                    }
                    if (!TextUtils.isEmpty(postalCode)) {
                        Tracker.getInstance().setZipCode(postalCode);
                    }
                }
            } catch(Exception e) {
                Log.d(TAG, "Error by Geocoder: " + e.toString());
                Crittercism.logHandledException(e);
            }
        }
    }

    // Getters
    public Location getLocation(@Nullable PostalCodeCallback callback) {
        // Try update
        if (client!=null) {
            Location latest = LocationServices.FusedLocationApi.getLastLocation(client);
            if ((latest != null) && latest != location) {
                Tracker.getInstance().trackLocation(latest); // analytics
                location = latest;
                new Resolver(callback).start();

                Log.d(TAG, "Current location -> Longitude:" + location.getLongitude()
                        + ", Latitude:" + location.getLatitude());
            }

            else {
                Log.d(TAG, "Default location -> Longitude:" + getDefaultLocation().getLongitude()
                        + ", Latitude:" + getDefaultLocation().getLatitude());
                if (callback != null) {
                    callback.onGetPostalCodeFailure();
                }
                return getDefaultLocation();
            }
        }

        return(location);
    }

    public Location getLocation() {
        return getLocation(null);
    }

    // use Framingham as a default location when location service is not available
    private Location getDefaultLocation(){
        Location defaultLocation = new Location("default");
        defaultLocation.setLatitude(DefaultLatitude);
        defaultLocation.setLongitude(DefaultLongitude);
        return defaultLocation;
    }

    public String getPostalCode() {
        return (postalCode);
    }

    // Callbacks

    @Override
    public void onConnected(Bundle connectionHint) {
        String msg = "GoogleApiClient connected in "+Long.toString(System.currentTimeMillis()-startTime)+"ms";
        Log.d(TAG, msg);
        connected = true;

        Location latest = LocationServices.FusedLocationApi.getLastLocation(client);
        if (latest!=null && latest!=location) {
            location = latest;
            new Resolver().start();
        }
    }

    @Override
    public void onConnectionSuspended(int cause) {
        Log.d(TAG, "GoogleApiClient suspended");
        connected = false;
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.d(TAG, "GoogleApiClient connect failed: "+result.toString());
        connected = false;
    }

    // Recent location

    public void loadRecentLocation() {
        SharedPreferences prefs = context.getSharedPreferences(MainActivity.PREFS_FILENAME, Context.MODE_PRIVATE);
        if (prefs.contains(PREFS_LATITUDE) && prefs.contains(PREFS_LONGITUDE)) {
            location = new Location(prefs.getString(PREFS_PROVIDER, "Unknown"));
            location.setLatitude(prefs.getFloat(PREFS_LATITUDE, 0.0f));
            location.setLongitude(prefs.getFloat(PREFS_LONGITUDE, 0.0f));
            location.setTime(prefs.getLong(PREFS_TIMESTAMP, 0));
        }
        if (prefs.contains(PREFS_POSTALCODE)) {
            postalCode = prefs.getString(PREFS_POSTALCODE, null);
            Tracker.getInstance().setZipCode(postalCode);
        }
    }

    public void saveRecentLocation() {
        SharedPreferences prefs = context.getSharedPreferences(MainActivity.PREFS_FILENAME, Context.MODE_PRIVATE);
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
