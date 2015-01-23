package com.staples.mobile.cfa.profile;

import android.app.Activity;
import android.content.Context;
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

public class PlacesArrayAdapter extends ArrayAdapter<String> implements Filterable {

    private static final String TAG = "PlacesArrayAdapter";

    private static final boolean LOGGING = true;

    private static final String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/place";
    private static final String TYPE_AUTOCOMPLETE = "/autocomplete";
    private static final String TYPE_DETAILS = "/details";
    private static final String OUT_JSON = "/json";

    // @@@ TODO Key copied from the manifest?
    private static final String API_KEY = "AIzaSyCGiUC4JbomlAzTDXqgFiFJPf45Ckux-Rs";

    private static final int STREET_ADDRESS = 0;
    private static final int CITY = 1;
    private static final int STATE = 2;

    private Activity activity;

    private JSONArray predictionsJsonArray;

    private JSONArray addressCompsJsonArray;

    private ArrayList<String> resultList;

    private String placeId;

    private PlaceData placeData;

    private PlaceDataCallback placeDataCallback;

    public interface PlaceDataCallback {

        public void onPlaceDataResult(PlacesArrayAdapter.PlaceData placeData);
    }

    private Runnable getZipCodeRunnable = new Runnable() {

        public void run() {

            // <<<<< Runs on a worker thread. >>>>>

            if (LOGGING) Log.v(TAG, "PlacesArrayAdapter:getZipCodeRunnable:Run():"
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
                    Log.e(TAG, "PlacesArrayAdapter:getZipCodeRunnable:Run(): EXCEPTION[MalformedURLException]: Error processing Places API URL."
                                    + " malformedURLException[" + malformedURLException + "]"
                                    + " this[" + this + "]"
                    );
                return;

            } catch (IOException ioException) {

                if (LOGGING)
                    Log.e(TAG, "PlacesArrayAdapter:getZipCodeRunnable:Run(): EXCEPTION[IOException]: Error connecting to Places API."
                                    + " ioException[" + ioException + "]"
                                    + " this[" + this + "]"
                    );
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
                        }
                    }
                }

            } catch (JSONException jsonException) {

                if (LOGGING)
                    Log.e(TAG, "PlacesArrayAdapter:getZipCodeRunnable:Run(): EXCEPTION[JSONException]: Cannot process JSON results."
                                    + " jsonException[" + jsonException + "]"
                                    + " this[" + this + "]"
                    );
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
    }

    public void getPlaceDetails(int placeIndex, PlaceDataCallback placeDataCallback) {

        if (LOGGING) Log.v(TAG, "PlacesArrayAdapter:getPlaceDetails():"
                        + " this[" + this + "]"
        );

        this.placeDataCallback = placeDataCallback;

        JSONObject placeJsonObject = null;
        JSONArray termsJsonArray = null;
        JSONObject termJsonObject = null;

        placeData = new PlaceData();

        try {

            placeJsonObject = predictionsJsonArray.getJSONObject(placeIndex);

            placeId = placeJsonObject.getString("place_id");

            termsJsonArray = placeJsonObject.getJSONArray("terms");

            for (int termIndex = 0; termIndex <= STATE; termIndex++) {

                termJsonObject = termsJsonArray.getJSONObject(termIndex);

                if (termIndex == STREET_ADDRESS) {

                    placeData.streetAddress = termJsonObject.getString("value");

                } else if (termIndex == CITY) {

                    placeData.city = termJsonObject.getString("value");

                } else if (termIndex == STATE) {

                    placeData.state = termJsonObject.getString("value");
                }
            }

            runGetZipCode();

        } catch (JSONException jsonException) {
            // @@@ TODO Must handle.
            placeData = null;
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
                    resultList = autocomplete(constraint.toString());

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

        Thread getZipCodeThread = new Thread(getZipCodeRunnable);

        getZipCodeThread.start();
    }

    private ArrayList<String> autocomplete(String input) {

        // <<<<< Runs on a worker thread. >>>>>

        if (LOGGING) Log.v(TAG, "PlacesArrayAdapter:autocomplete():"
                        + " input[" + input + "]"
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
            stringBuilder.append("&input=" + URLEncoder.encode(input, "utf8"));

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
                Log.e(TAG, "PlacesArrayAdapter:autocomplete(): EXCEPTION[MalformedURLException]: Error processing Places API URL."
                                + " malformedURLException[" + malformedURLException + "]"
                                + " this[" + this + "]"
                );
            return (resultList);

        } catch (IOException ioException) {

            if (LOGGING)
                Log.e(TAG, "PlacesArrayAdapter:autocomplete(): EXCEPTION[IOException]: Error connecting to Places API."
                                + " ioException[" + ioException + "]"
                                + " this[" + this + "]"
                );
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

        } catch (JSONException jsonException) {

            if (LOGGING)
                Log.e(TAG, "PlacesArrayAdapter:autocomplete(): EXCEPTION[JSONException]: Cannot process JSON results."
                                + " jsonException[" + jsonException + "]"
                                + " this[" + this + "]"
                );
        }

        return (resultList);
    }

    public class PlaceData {

        public String streetAddress = "";
        public String city = "";
        public String state = "";
        public String zipCode = "";
        public String zipCodeSuffix = "";
    }
}
