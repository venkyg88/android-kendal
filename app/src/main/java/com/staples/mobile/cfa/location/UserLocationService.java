package com.staples.mobile.cfa.location;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;

import com.google.android.gms.maps.model.LatLng;
import com.staples.mobile.cfa.location.LatLngService.LatLngService;
import com.staples.mobile.cfa.location.LatLngService.LatLngServiceCallBack;

import java.util.List;
import java.util.Locale;


/**
 * Created by parve002 on 11/24/14.
 */


public class UserLocationService implements LatLngServiceCallBack{

    private UserZipCodeCallBack userZipCodeCallBack;
    private UserLatLngCallBack userLatLngCallBack;

    public static final String PREFS_NAME = "MyPrefsFile";
    Context context;

    /**
     * Use this constructor for preparing UserLocationService to request an on demand zipcode of the user.
     * @param context is the current Context of the caller.
     * @param userZipCodeCallBack is reference to caller.
     */
    public UserLocationService(Context context, UserZipCodeCallBack userZipCodeCallBack) {
        this.context = context;
        this.userZipCodeCallBack = userZipCodeCallBack;
        this.userLatLngCallBack = null;
    }

    /**
     * Use this constructor for preparing UserLocationService to request an on demand Latitude and Longitude of the user.
     * @param context is the current Context of the caller.
     * @param userLatLngCallBack is reference to caller.
     */
    public UserLocationService( Context context, UserLatLngCallBack userLatLngCallBack) {
        this.context = context;
        this.userZipCodeCallBack = null;
        this.userLatLngCallBack = userLatLngCallBack;
    }


    /**
     * This function initiates an on demand location request of the User and returns 5 digit ZipCode string or LatLng Object based on the constructor used.
     */
    public void getUserLocation() {
        LatLngService latLngService = new LatLngService(this.context, this);
    }

    /**
     * This function must only be used for setting user location during app launch. The LatLng and ZipCode(Obtained from geocoding LatLng) is added to SharedPreferences.
     * @param currentLatLng is the Latitude and Longitude values obtained during app stepup.
     * @param context is the Context of the caller.
     */
    public static void setCachedUserLocation(LatLng currentLatLng, Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        sharedPref.edit().putFloat("Latitude", (float) currentLatLng.latitude);
        sharedPref.edit().putFloat("Longitude", (float) currentLatLng.longitude);
        String zipcode = geoCodeLatLng(context, currentLatLng);
        if (zipcode == null){
            //If failed to geocode user Latitude and longitude setting the default to Framingham, MA.
            sharedPref.edit().putString("ZipCode", "01702");
        } else {
            sharedPref.edit().putString("ZipCode", zipcode);
        }
        sharedPref.edit().commit();
    }


    /**
     * This Function returns the Zipcode during initial all setup. Must only used for the esay open API's, Analytics and worst case zipcode fall back scenario.
     * @param context is the Context of the caller.
     * @return 5 digit ZipCode stored as string from Shared Preferences. If not found in SharedPreferences will retun null.
     */
    public static String getCachedZipCode(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return sharedPref.getString("ZipCode", null);
    }

    /**
     * This function returns the Latitude and Longitude during initial all setup. Must only used for easy open API's the url, Analytics and worst case zipcode
     * fall back scenario.
     * @param context is the Context of the caller.
     * @return 5 digit ZipCode stored as string from Shared Preferences. If not found in SharedPreferences will retun null.
     */
    public static LatLng getCachedLatLng(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        double latitude = sharedPref.getFloat("Latitude", 0);
        double longitude = sharedPref.getLong("Longitude", 0);
        if (latitude == 0 && longitude == 0) {
            return null;
        } else {
            LatLng latLng = new LatLng(latitude, longitude);
            return latLng;
        }
    }

    /**
     * This function geocodes the input latitude and longitude into zipcode.
     * @param latLng is the LatLng object to be geocoded.
     * @param context is the Context of the caller.
     * @return 5 digit ZipCode if successfully geocoded else returns null.
     */
    private static String geoCodeLatLng(Context context, LatLng latLng) {
        if (latLng != null) {

            Geocoder geocoder = new Geocoder(context, Locale.getDefault());
            try {
                List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
                if (addresses != null) {
                    if (addresses.get(0).getPostalCode() != null) {
                        String zipCode = addresses.get(0).getPostalCode();
                        return zipCode;
                    }
                }
            } catch (Exception e) {

            }
        }
        return null;
    }

    @Override
    public void onLatLngServiceCallBack(LatLng latLng) {
        if (this.userLatLngCallBack != null) {
            this.userLatLngCallBack.onUserLatLngCallBack(latLng);
        } else if (this.userZipCodeCallBack != null) {
            String zipcode = geoCodeLatLng(this.context, latLng);
            this.userZipCodeCallBack.onUserZipCodeCallBack(zipcode);
        } else {
            //TODO: throw error
        }
    }
}
