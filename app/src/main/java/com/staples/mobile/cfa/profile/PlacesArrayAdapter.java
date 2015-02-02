package com.staples.mobile.cfa.profile;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

import com.staples.mobile.cfa.R;

public class PlacesArrayAdapter extends ArrayAdapter<String> implements Filterable {

    private static final String TAG = "PlacesArrayAdapter";

    private static final boolean LOGGING = false;

    private static final String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/place";
    private static final String TYPE_AUTOCOMPLETE = "/autocomplete";
    private static final String TYPE_DETAILS = "/details";
    private static final String OUT_JSON = "/json";

    // @@@ TODO Key copied from the manifest?
    private static final String API_KEY = "AIzaSyCGiUC4JbomlAzTDXqgFiFJPf45Ckux-Rs";

    public enum ERROR_CODE {
        NONE,
        EXCEPTION,
    }

    private Activity activity;
    private Resources resources;

    private JSONArray predictionsJsonArray;
    private JSONArray addressCompsJsonArray;

    private ArrayList<String> resultList;

    private String placeId;

    private PlaceData placeData;
    private PlaceDataCallback placeDataCallback;

    public interface PlaceDataCallback {

        public void onPlaceDataResult(PlacesArrayAdapter.PlaceData placeData);
    }

    private Runnable getPlaceDetailsRunnable = new Runnable() {

        public void run() {

            // <<<<< Runs on a worker thread. >>>>>

            if (LOGGING) Log.v(TAG, "PlacesArrayAdapter:getPlaceDetailsRunnable:Run():"
                            + " placeId[" + placeId + "]"
                            + " this[" + this + "]"
            );

            HttpURLConnection httpURLConnection = null;
            StringBuilder jsonResults = new StringBuilder();

            try {
                StringBuilder stringBuilder = new StringBuilder(PLACES_API_BASE + TYPE_DETAILS + OUT_JSON);
                stringBuilder.append("?placeid=" + placeId);
                stringBuilder.append("&key=" + API_KEY);

                URL url = new URL(stringBuilder.toString());
                httpURLConnection = (HttpURLConnection) url.openConnection();
                InputStreamReader inputStreamReader = new InputStreamReader(httpURLConnection.getInputStream());

                // Load the results into a StringBuilder
                int nbrOfCharactersRead = 0;
                char[] resultsBuffer = new char[1024];

                while ((nbrOfCharactersRead = inputStreamReader.read(resultsBuffer)) != -1) {
                    jsonResults.append(resultsBuffer, 0, nbrOfCharactersRead);
                }
            } catch (MalformedURLException malformedURLException) {

                if (LOGGING)
                    Log.e(TAG, "PlacesArrayAdapter:getPlaceDetailsRunnable:Run(): EXCEPTION[MalformedURLException]: Error processing Places API URL."
                                    + " malformedURLException[" + malformedURLException + "]"
                                    + " this[" + this + "]"
                    );

                placeData.errorCode = ERROR_CODE.EXCEPTION;
                placeData.exception = malformedURLException;

                return;

            } catch (IOException ioException) {

                if (LOGGING)
                    Log.e(TAG, "PlacesArrayAdapter:getPlaceDetailsRunnable:Run(): EXCEPTION[IOException]: Error connecting to Places API."
                                    + " ioException[" + ioException + "]"
                                    + " this[" + this + "]"
                    );

                placeData.errorCode = ERROR_CODE.EXCEPTION;
                placeData.exception = ioException;

                return;

            } finally {

                if (httpURLConnection != null) {
                    httpURLConnection.disconnect();
                }
            }

            JSONObject documentJsonObj = null;
            JSONObject resultJsonObj = null;
            int addressCompsNbrOfItems = 0;

            JSONArray addressCompTypesJsonArray = null;
            int addressCompsTypesNbrOfItems = 0;
            JSONObject addressCompJsonObj = null;

            String addrCompTypeString = null;

            try {
                // Create a JSON object hierarchy from the results.
                documentJsonObj = new JSONObject(jsonResults.toString());

                resultJsonObj = documentJsonObj.getJSONObject("result");
                addressCompsJsonArray = resultJsonObj.getJSONArray("address_components");
                addressCompsNbrOfItems = addressCompsJsonArray.length();

                placeData.clear();

                for (int addrCompIndex = 0; addrCompIndex < addressCompsNbrOfItems; addrCompIndex++) {

                    addressCompJsonObj = addressCompsJsonArray.getJSONObject(addrCompIndex);
                    addressCompTypesJsonArray = addressCompJsonObj.getJSONArray("types");
                    addressCompsTypesNbrOfItems = addressCompTypesJsonArray.length();

                    for (int addrCompTypeIndex = 0; addrCompTypeIndex < addressCompsTypesNbrOfItems; addrCompTypeIndex++) {

                        addrCompTypeString = addressCompTypesJsonArray.getString(addrCompTypeIndex);

                        if ("postal_code".equals(addrCompTypeString)) {
                            placeData.zipCode = addressCompJsonObj.getString("short_name");
                        } else if ("postal_code_suffix".equals(addrCompTypeString)) {
                            placeData.zipCodeSuffix = addressCompJsonObj.getString("short_name");
                        } else if ("street_number".equals(addrCompTypeString)) {
                            placeData.streetNumber = addressCompJsonObj.getString("short_name");
                        } else if ("route".equals(addrCompTypeString)) {
                            placeData.streetName = addressCompJsonObj.getString("short_name");
                        } else if ("locality".equals(addrCompTypeString)) {
                            placeData.city = addressCompJsonObj.getString("long_name");
                        } else if ("sublocality".equals(addrCompTypeString)) {
                            placeData.city = addressCompJsonObj.getString("long_name");
                        } else if ("administrative_area_level_1".equals(addrCompTypeString)) {
                            placeData.state = addressCompJsonObj.getString("short_name");
                        }
                    }

                    String streetAddress = "";

                    if (placeData.streetNumber.length() > 0) {
                        streetAddress = placeData.streetNumber;
                    }

                    if (placeData.streetName.length() > 0) {
                        streetAddress += (" " + placeData.streetName);
                    }

                    placeData.streetAddress = streetAddress;
                }

            } catch (JSONException jsonException) {

                if (LOGGING)
                    Log.e(TAG, "PlacesArrayAdapter:getPlaceDetailsRunnable:Run(): EXCEPTION[JSONException]: Cannot process JSON results."
                                    + " jsonException[" + jsonException + "]"
                                    + " this[" + this + "]"
                    );

                placeData.errorCode = ERROR_CODE.EXCEPTION;
                placeData.exception = jsonException;

            } finally {

                if (placeDataCallback != null) {

                    activity.runOnUiThread(new Runnable() {

                        // <<<<< Runs on the UI thread. >>>>>

                        @Override
                        public void run() {

                            placeDataCallback.onPlaceDataResult(placeData);
                        }
                    }); // runOnUiThread
                }
            }

            return;

        } // run()
    };

    public PlacesArrayAdapter(Context context, int textViewResourceId) {

        super(context, textViewResourceId);

        activity = (Activity) context;
        resources = activity.getResources();

        placeData = new PlaceData();
    }

    public void getPlaceDetails(int placeIndex, PlaceDataCallback placeDataCallback) {

        if (LOGGING) Log.v(TAG, "PlacesArrayAdapter:getPlaceDetails():"
                        + " this[" + this + "]"
        );

        this.placeDataCallback = placeDataCallback;

        JSONObject placeJsonObject = null;
        JSONArray termsJsonArray = null;
        JSONObject termJsonObject = null;

        try {
            placeJsonObject = predictionsJsonArray.getJSONObject(placeIndex);
            placeId = placeJsonObject.getString("place_id");

            runGetZipCode();

        } catch (JSONException jsonException) {

            if (LOGGING)
                Log.e(TAG, "PlacesArrayAdapter:getPlaceDetails(): EXCEPTION[JSONException]: Cannot process JSON results."
                                + " jsonException[" + jsonException + "]"
                                + " this[" + this + "]"
                );

            placeData.errorCode = ERROR_CODE.EXCEPTION;
            placeData.exception = jsonException;
        }
    }

    @Override
    public int getCount() {

        int resultListSize = resultList.size();

        if (LOGGING) Log.v(TAG, "PlacesArrayAdapter:getCount():"
                        + " resultListSize[" + resultListSize + "]"
                        + " this[" + this + "]"
        );
        return (resultListSize);
    }

    @Override
    public String getItem(int index) {

        String item = resultList.get(index);

        if (LOGGING) Log.v(TAG, "PlacesArrayAdapter:getItem():"
                        + " item[" + item + "]"
                        + " this[" + this + "]"
        );
        return (item);
    }

    @Override
    public Filter getFilter() {

        Filter filter = new Filter() {

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {

                // <<<<< Runs on a worker thread. >>>>>

                if (LOGGING) Log.v(TAG, "PlacesArrayAdapter:Filter:performFiltering():"
                                + " constraint[" + constraint + "]"
                                + " this[" + this + "]"
                );

                FilterResults filterResults = new FilterResults();

                if (constraint != null) {

                    // Retrieve the autocomplete results.
                    resultList = autoComplete(constraint.toString());

                    // Assign the data to the FilterResults.
                    filterResults.values = resultList;
                    filterResults.count = resultList.size();
                }
                return (filterResults);
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {

                // <<<<< Runs on the UI thread. >>>>>

                if (LOGGING) Log.v(TAG, "PlacesArrayAdapter:Filter:publishResults():"
                                + " constraint[" + constraint + "]"
                                + " this[" + this + "]"
                );

                if (results != null && results.count > 0) {
                    notifyDataSetChanged();
                } else {
                    notifyDataSetInvalidated();
                }
            }
        };

        return (filter);
    }

    private void runGetZipCode() {

        Thread getZipCodeThread = new Thread(getPlaceDetailsRunnable);

        getZipCodeThread.start();
    }

    private ArrayList<String> autoComplete(String constraint) {

        // <<<<< Runs on a worker thread. >>>>>

        if (LOGGING) Log.v(TAG, "PlacesArrayAdapter:autoComplete():"
                        + " constraint[" + constraint + "]"
                        + " this[" + this + "]"
        );

        ArrayList<String> resultList = null;

        HttpURLConnection httpURLConnection = null;
        StringBuilder jsonResults = new StringBuilder();

        try {

            StringBuilder stringBuilder = new StringBuilder(PLACES_API_BASE + TYPE_AUTOCOMPLETE + OUT_JSON);
            stringBuilder.append("?key=" + API_KEY);
            stringBuilder.append("&types=address");
            stringBuilder.append("&components=country:us");
            stringBuilder.append("&input=" + URLEncoder.encode(constraint, "utf8"));

            URL url = new URL(stringBuilder.toString());
            httpURLConnection = (HttpURLConnection) url.openConnection();
            InputStreamReader inputStreamReader = new InputStreamReader(httpURLConnection.getInputStream());

            // Load the results into a StringBuilder
            int nbrOfCharactersRead = 0;
            char[] resultsBuffer = new char[1024];

            while ((nbrOfCharactersRead = inputStreamReader.read(resultsBuffer)) != -1) {
                jsonResults.append(resultsBuffer, 0, nbrOfCharactersRead);
            }

        } catch (MalformedURLException malformedURLException) {

            if (LOGGING)
                Log.e(TAG, "PlacesArrayAdapter:autoComplete(): EXCEPTION[MalformedURLException]: Error processing Places API URL."
                                + " malformedURLException[" + malformedURLException + "]"
                                + " this[" + this + "]"
                );

            placeData.errorCode = ERROR_CODE.EXCEPTION;
            placeData.exception = malformedURLException;

            return (resultList);

        } catch (IOException ioException) {

            if (LOGGING)
                Log.e(TAG, "PlacesArrayAdapter:autoComplete(): EXCEPTION[IOException]: Error connecting to Places API."
                                + " ioException[" + ioException + "]"
                                + " this[" + this + "]"
                );

            placeData.errorCode = ERROR_CODE.EXCEPTION;
            placeData.exception = ioException;

            return (resultList);

        } finally {

            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
        }

        try {

            // Create a JSON object hierarchy from the results
            JSONObject predictionsJsonObj = new JSONObject(jsonResults.toString());
            predictionsJsonArray = predictionsJsonObj.getJSONArray("predictions");

            // Extract the Place descriptions from the results
            resultList = new ArrayList<String>(predictionsJsonArray.length());

            for (int predsNdx = 0; predsNdx < predictionsJsonArray.length(); predsNdx++) {
                resultList.add(predictionsJsonArray.getJSONObject(predsNdx).getString("description"));
            }

            // @@@ TODO Add "INPUT MANUALLY" here. (input_manually_allcaps)

        } catch (JSONException jsonException) {

            if (LOGGING)
                Log.e(TAG, "PlacesArrayAdapter:autoComplete(): EXCEPTION[JSONException]: Cannot process JSON results."
                                + " jsonException[" + jsonException + "]"
                                + " this[" + this + "]"
                );

            placeData.errorCode = ERROR_CODE.EXCEPTION;
            placeData.exception = jsonException;
        }

        String inputManually = resources.getString(R.string.input_manually_allcaps);
        resultList.add(inputManually);

        return (resultList);
    }

    public static class PlaceData {

        public String streetAddress = "";
        public String streetNumber = "";
        public String streetName = "";
        public String city = "";
        public String state = "";
        public String zipCode = "";
        public String zipCodeSuffix = "";
        public ERROR_CODE errorCode = ERROR_CODE.NONE;
        public Exception exception = null;

        public void clear() {

            streetAddress = "";
            streetNumber = "";
            streetName = "";
            city = "";
            state = "";
            zipCode = "";
            zipCodeSuffix = "";
            errorCode = ERROR_CODE.NONE;
            exception = null;
        }
    }
}
