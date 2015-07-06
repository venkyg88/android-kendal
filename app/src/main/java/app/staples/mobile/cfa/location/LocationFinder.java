package app.staples.mobile.cfa.location;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.crittercism.app.Crittercism;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.staples.mobile.common.access.Access;
import com.staples.mobile.common.access.channel.model.store.StoreData;
import com.staples.mobile.common.access.channel.model.store.StoreQuery;
import com.staples.mobile.common.analytics.Tracker;

import java.util.ArrayList;
import java.util.List;

import app.staples.mobile.cfa.MainActivity;
import app.staples.mobile.cfa.store.StoreFragment;
import app.staples.mobile.cfa.store.StoreItem;
import app.staples.mobile.cfa.util.MiscUtils;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/** This singleton class performs a sequence of operations:
 *  Get GoogleApiClient -> get location -> get postal code -> get nearby store -> set preferred store
 */
public class LocationFinder implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, Callback<StoreQuery> {
    private static final String TAG = LocationFinder.class.getSimpleName();

    private static final String PREFS_PROVIDER = "locationProvider";
    private static final String PREFS_LATITUDE = "locationLatitude";
    private static final String PREFS_LONGITUDE = "locationLongitude";
    private static final String PREFS_TIMESTAMP = "locationTimestamp";
    private static final String PREFS_POSTALCODE = "postalCode";
    private static final String PREFS_NEARBYSTORE = "nearbyStore";
    private static final String PREFS_PREFERREDSTORE = "preferredStore";

    private static final float DEFAULT_LATITUDE = 42.2913142f;
    private static final float DEFAULT_LONGITUDE = -71.4888961f;
    private static final String DEFAULT_POSTALCODE = "01702";

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
    private long startTime;

    private Location location;
    private String postalCode;
    private StoreItem nearbyStore;
    private StoreItem preferredStore;

    private LocationFinder(Context context) {
        this.context = context;
        geocoder = new Geocoder(context);
        loadRecentLocation();
    }

    public boolean connect() {
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
                return(true);
            default:
                String msg = "Google Play Services: "+GooglePlayServicesUtil.getErrorString(status);
                Log.d(TAG, msg);
                return(false);
        }
    }

    // Getters

    public GoogleApiClient getClient() {
        return(client);
    }

    public Location getLocation() {
        return(location);
    }

    public String getPostalCode() {
        return(postalCode);
    }

    public StoreItem getNearestStore() {
        return(nearbyStore);
    }

    public StoreItem getPreferredStore() {
        return(preferredStore);
    }

    // Google API callbacks

    @Override
    public void onConnected(Bundle connectionHint) {
        String msg = "GoogleApiClient connected in "+Long.toString(System.currentTimeMillis()-startTime)+"ms";
        Log.d(TAG, msg);
        connected = true;

        // Resolve actual location to postal code
        Location latest = LocationServices.FusedLocationApi.getLastLocation(client);
        if (latest!=null) {
            location = latest;
            new Resolver().start();
        }

        // Get nearest store based on saved postal code
        else {
            Access.getInstance().getChannelApi(false).storeLocations(postalCode, LocationFinder.this);
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

    // Geocoder address resolver

    private class Resolver extends Thread {
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
                    if (!TextUtils.isEmpty(postalCode)) {
                        Tracker.getInstance().setZipCode(postalCode);
                        Access.getInstance().getChannelApi(false).storeLocations(postalCode, LocationFinder.this);
                    }
                }
            } catch(Exception e) {
                Log.d(TAG, "Error by Geocoder: " + e.toString());
                Crittercism.logHandledException(e);
            }
        }
    }

    // Nearest store Retrofit callbacks

    @Override
    public void success(StoreQuery storeQuery, Response response) {
        if (storeQuery==null) return;
        List<StoreData> storeDatas = storeQuery.getStoreData();
        if (storeDatas==null || storeDatas.size()==0) return;
        StoreItem item = StoreFragment.processStoreData(storeDatas.get(0));
        if (item!=null) {
            nearbyStore = item;
            if (context instanceof MainActivity) {
                ((MainActivity) context).onNearbyStore();
            }
            Log.d(TAG, "Nearest store "+nearbyStore.streetAddress1);
        }
    }

    @Override
    public void failure(RetrofitError retrofitError) {
    }

    // Cached values in preferences

    private StoreItem loadStoreItem(SharedPreferences prefs, String key) {
        List<String> list = MiscUtils.multiStringToList(prefs.getString(key, null));
        if (list==null || list.size()!=6) return(null);
        StoreItem item = new StoreItem();
        item.storeNumber = list.get(0);
        item.streetAddress1 = list.get(1);
        item.streetAddress2 = list.get(2);
        item.city = list.get(3);
        item.state = list.get(4);
        item.postalCode = list.get(5);
        return(item);
    }

    private void saveStoreItem(SharedPreferences.Editor editor, String key, StoreItem item) {
        if (item==null) return;
        List<String> list = new ArrayList<String>(6);
        list.add(item.storeNumber);
        list.add(item.streetAddress1);
        list.add(item.streetAddress2);
        list.add(item.city);
        list.add(item.state);
        list.add(item.postalCode);
        String multi = MiscUtils.listToMultiString(list);
        editor.putString(key, multi);
    }

    private void loadRecentLocation() {
        SharedPreferences prefs = context.getSharedPreferences(MainActivity.PREFS_FILENAME, Context.MODE_PRIVATE);

        location = new Location(prefs.getString(PREFS_PROVIDER, "Default"));
        location.setLatitude(prefs.getFloat(PREFS_LATITUDE, DEFAULT_LATITUDE));
        location.setLongitude(prefs.getFloat(PREFS_LONGITUDE, DEFAULT_LONGITUDE));
        location.setTime(prefs.getLong(PREFS_TIMESTAMP, 0));

        postalCode = prefs.getString(PREFS_POSTALCODE, DEFAULT_POSTALCODE);

        nearbyStore = loadStoreItem(prefs, PREFS_NEARBYSTORE);
        preferredStore = loadStoreItem(prefs, PREFS_PREFERREDSTORE);
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

        saveStoreItem(editor, PREFS_NEARBYSTORE, nearbyStore);
        saveStoreItem(editor, PREFS_PREFERREDSTORE, preferredStore);

        editor.apply();
    }
}
