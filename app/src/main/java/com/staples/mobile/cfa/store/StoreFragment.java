package com.staples.mobile.cfa.store;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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

import java.util.ArrayList;

public class StoreFragment extends Fragment implements GoogleMap.OnMarkerClickListener{
    private static final String TAG = "StoreFragment";

    private static final LatLng VELOCITY = new LatLng(42.3672799,-71.0900776);

    private MapView mapView;
    private ArrayList<Store> stores;
    private int width;
    private int height;

    private class Store {
        int id;
        private String title;
        private LatLng position;
        private Marker marker;

        private Store(int id, String title, double latitude, double longitude) {
            this.id = id;
            this.title = title;
            position = new LatLng(latitude, longitude);
        }

        private void addMarker(GoogleMap googleMap) {
            MarkerOptions marker = new MarkerOptions();
            marker.title(title);
            marker.position(position);
            googleMap.addMarker(marker);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        width = container.getWidth(); // TODO hacked;
        height = container.getHeight();

        View view =inflater.inflate(R.layout.store_fragment, container, false);
        mapView = (MapView) view.findViewById(R.id.map);
        mapView.onCreate(bundle);

        GoogleMap googleMap = mapView.getMap();
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        googleMap.setMyLocationEnabled(true);
        googleMap.setOnMarkerClickListener(this);

        stores = new ArrayList<Store>();
        stores.add(new Store(1, "Velocity Lab", 42.3672799, -71.0900776));
        stores.add(new Store(2, "Harvard Square", 42.3721032, -71.1207659));
        stores.add(new Store(3, "Fenway", 42.3446884, -71.1032183));
        stores.add(new Store(4, "Allston", 42.3496638, -71.1299881));
        stores.add(new Store(5, "Boston", 42.3592204, -71.0580453));

        for(Store store : stores)
            store.addMarker(googleMap);

        scaleMap();

        return(view);
    }

    private void scaleMap() {
        double centerLat = VELOCITY.latitude;
        double centerLng = VELOCITY.longitude;
        double deltaLat = 0.0; // TODO minimum values
        double deltaLng = 0.0;

        // Collect maximum deltas
        for(Store store : stores) {
            deltaLat = Math.max(deltaLat, Math.abs(store.position.latitude-centerLat));
            deltaLng = Math.max(deltaLng, Math.abs(store.position.longitude-centerLng));
        }

        // Make bounds
        LatLng northeast = new LatLng(centerLat+deltaLat, centerLng+deltaLng);
        LatLng southwest = new LatLng(centerLat-deltaLat, centerLng-deltaLng);
        LatLngBounds bounds = new LatLngBounds(southwest, northeast);

        // Zoom to bounds
        MapsInitializer.initialize(getActivity());
        CameraUpdate update = CameraUpdateFactory.newLatLngBounds(bounds, width, height, 50);
        GoogleMap googleMap = mapView.getMap();
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

    public boolean onMarkerClick(Marker marker) {
        LatLng location = marker.getPosition();
        for(Store store : stores) {
            if (store.position.latitude==location.latitude &&
                store.position.longitude==location.longitude) {
                String text = store.title + "\n" + "Store #" + store.id;
                ((TextView) getView().findViewById(R.id.title)).setText(text);
                return(false);
            }
        }
        return(false);
    }
}
