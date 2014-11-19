package com.staples.mobile.cfa.store;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.staples.mobile.R;
import com.staples.mobile.common.access.Access;
import com.staples.mobile.common.access.channel.model.store.Obj;
import com.staples.mobile.common.access.channel.model.store.StoreAddress;
import com.staples.mobile.common.access.channel.model.store.StoreData;
import com.staples.mobile.common.access.channel.model.store.StoreQuery;
import com.staples.mobile.common.access.easyopen.model.ApiError;

import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class StoreFragment extends Fragment implements Callback<StoreQuery>, GoogleMap.OnMarkerClickListener{
    private static final String TAG = "StoreFragment";

    private MapView mapView;
    private GoogleMap googleMap;
    private ListView list;
    private StoreAdapter adapter;

    private double centerLat = 42.3672799; // Velocity lab
    private double centerLng = -71.0900776;
    private double deltaLat;
    private double deltaLng;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        View view = inflater.inflate(R.layout.store_fragment, container, false);
        mapView = (MapView) view.findViewById(R.id.map);
        list = (ListView) view.findViewById(R.id.list);
        adapter = new StoreAdapter(getActivity());
        list.setAdapter(adapter);

        // Supports Play Services?
        if (GooglePlayServicesUtil.isGooglePlayServicesAvailable(getActivity())==0) {
            mapView.onCreate(bundle);

            googleMap = mapView.getMap();
            googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            googleMap.setMyLocationEnabled(true);
            googleMap.setOnMarkerClickListener(this);

            MapsInitializer.initialize(getActivity());
            CameraUpdate update = CameraUpdateFactory.newLatLng(new LatLng(centerLat, centerLng));
            googleMap.moveCamera(update);
        }

        else mapView.setVisibility(View.GONE);

        Access.getInstance().getChannelApi().storeLocations("02139", this);
        return (view);
    }

    private void expandBounds(StoreItem item) {
        deltaLat = Math.max(deltaLat, Math.abs(item.position.latitude-centerLat));
        deltaLng = Math.max(deltaLng, Math.abs(item.position.longitude-centerLng));
    }

    private void addMarker(StoreItem item) {
        MarkerOptions options = new MarkerOptions();
        options.title(item.streetAddress1);
        options.position(item.position);
        googleMap.addMarker(options);
    }

    private void scaleMap() {
        // Inflate deltas to give margins
        deltaLat *= 1.1f;
        deltaLng *= 1.1f;

        // Make bounds
        LatLng northeast = new LatLng(centerLat+deltaLat, centerLng+deltaLng);
        LatLng southwest = new LatLng(centerLat-deltaLat, centerLng-deltaLng);
        LatLngBounds bounds = new LatLngBounds(southwest, northeast);

        // Get map dimensions
        int width = mapView.getWidth();
        int height = mapView.getHeight();

        // Zoom to bounds
        CameraUpdate update = CameraUpdateFactory.newLatLngBounds(bounds, width, height, 0);
        googleMap.moveCamera(update);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mapView!=null) mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mapView!=null) mapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mapView!=null) mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (mapView!=null) mapView.onLowMemory();
    }

    private StoreItem addStore(StoreData storeData) {
        if (storeData==null) return(null);
        Obj obj = storeData.getObj();
        if (obj==null) return(null);

        // Get coordinates
        List<Double> loc = obj.getLoc();
        if (loc==null || loc.size()!=2) return(null);
        Double lat = loc.get(1);
        Double lng = loc.get(0);
        if (lat==null || lng==null) return(null);

        String storeNumber = obj.getStoreNumber();
        StoreItem item = new StoreItem(storeNumber, lat, lng);

        // Get store address
        StoreAddress storeAddress = obj.getStoreAddress();
        if (storeAddress!=null) {
            item.streetAddress1 = storeAddress.getAddressLine1();
            item.streetAddress2 = storeAddress.getAddressLine2();
            item.city = storeAddress.getCity();
            item.state = storeAddress.getState();
            item.country = storeAddress.getCountry();
            item.zipcode = storeAddress.getZip();
        }

        adapter.addStore(item);
        if (googleMap!=null)
            addMarker(item);

        return(item);
    }

    @Override
    public void success(StoreQuery storeQuery, Response response) {
        Activity activity = getActivity();
        if (activity==null) return;

        if (storeQuery==null) return;
        List<StoreData> storeDatas = storeQuery.getStoreData();
        if (storeDatas==null) return;

        // Set maximum zoom
        deltaLat = 0.02;
        deltaLng = 0.02/Math.cos(Math.PI/180.0*centerLat);

        // Add stores, but fit only the first 5 in view
        int count = 0;
        for(StoreData storeData : storeDatas) {
            StoreItem item = addStore(storeData);
            if (count<5) expandBounds(item);
            count++;
        }

        // Set initial display
        if (googleMap!=null)
            adapter.setSingleMode(true);
        adapter.setSingleIndex(0);
        adapter.notifyDataSetChanged();
        if (googleMap!=null)
           scaleMap();
    }

    @Override
    public void failure(RetrofitError retrofitError) {
        Activity activity = getActivity();
        if (activity==null) return;

        String msg = ApiError.getErrorMessage(retrofitError);
        Toast.makeText(activity, msg, Toast.LENGTH_LONG).show();
        Log.d(TAG, msg);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        // Find store item by matching LatLng
        LatLng location = marker.getPosition();
        int index = adapter.findPositionByLatLng(location);
        if (index<0) return(false);

        if (adapter.isSingleMode()) {
            adapter.setSingleIndex(index);
            adapter.notifyDataSetChanged();
        }
        return(true);
    }
}
