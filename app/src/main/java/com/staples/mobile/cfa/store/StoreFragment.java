package com.staples.mobile.cfa.store;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

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

import java.util.ArrayList;
import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class StoreFragment extends Fragment implements Callback<StoreQuery>, GoogleMap.OnMarkerClickListener{
    private static final String TAG = "StoreFragment";

    private MapView mapView;
    private GoogleMap googleMap;
    private ArrayList<Store> stores;

    private double centerLat = 42.3672799; // Velocity lab
    private double centerLng = -71.0900776;
    private double deltaLat;
    private double deltaLng;

    private class Store {
        private LatLng position;
        private String storeNumber;
        private String streetAddress1;
        private String streetAddress2;
        private String city;
        private String state;
        private String country;
        private String zipcode;
        private String phoneNumber;

        private Store(String storeNumber, double latitude, double longitude) {
            this.storeNumber = storeNumber;
            position = new LatLng(latitude, longitude);
        }

        private void expandBounds() {
            deltaLat = Math.max(deltaLat, Math.abs(position.latitude-centerLat));
            deltaLng = Math.max(deltaLng, Math.abs(position.longitude-centerLng));
        }

        private void addMarker() {
            MarkerOptions options = new MarkerOptions();
            options.title(streetAddress1);
            options.position(position);
            googleMap.addMarker(options);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        View view =inflater.inflate(R.layout.store_fragment, container, false);
        mapView = (MapView) view.findViewById(R.id.map);
        mapView.onCreate(bundle);

        googleMap = mapView.getMap();
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        googleMap.setMyLocationEnabled(true);
        googleMap.setOnMarkerClickListener(this);

        stores = new ArrayList<Store>();

        MapsInitializer.initialize(getActivity());
        CameraUpdate update = CameraUpdateFactory.newLatLng(new LatLng(centerLat, centerLng));
        googleMap.moveCamera(update);

        Access.getInstance().getChannelApi().storeLocations("02139", this);

        return(view);
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
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    private Store addStore(StoreData storeData) {
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
        Store store = new Store(storeNumber, lat, lng);
        stores.add(store);

        // Get store address
        StoreAddress storeAddress = obj.getStoreAddress();
        if (storeAddress!=null) {
            store.streetAddress1 = storeAddress.getAddressLine1();
            store.streetAddress2 = storeAddress.getAddressLine2();
            store.city = storeAddress.getCity();
            store.state = storeAddress.getState();
            store.country = storeAddress.getCountry();
            store.zipcode = storeAddress.getZip();
        }

        store.addMarker();

        return(store);
    }

    @Override
    public void success(StoreQuery storeQuery, Response response) {
        Log.d(TAG, "Callback success");
        if (storeQuery==null) return;
        List<StoreData> storeDatas = storeQuery.getStoreData();
        if (storeDatas==null) return;

        deltaLat = 0.02;
        deltaLng = 0.02/Math.cos(Math.PI/180.0*centerLat);
        int count = 0;
        for(StoreData storeData : storeDatas) {
            Store store = addStore(storeData);
            if (count<5) store.expandBounds();
            count++;
        }

        scaleMap();
    }

    @Override
    public void failure(RetrofitError retrofitError) {
        String msg = ApiError.getErrorMessage(retrofitError);
        Toast.makeText(getActivity(), msg, Toast.LENGTH_LONG).show();
    }

    public boolean onMarkerClick(Marker marker) {
        LatLng location = marker.getPosition();
        for(Store store : stores) {
            if (store.position.latitude==location.latitude &&
                store.position.longitude==location.longitude) {
                String text = store.city+"\n"+store.streetAddress1 + "\n" + "Store #" + store.storeNumber;
                ((TextView) getView().findViewById(R.id.title)).setText(text);
                return(false);
            }
        }
        return(false);
    }
}
