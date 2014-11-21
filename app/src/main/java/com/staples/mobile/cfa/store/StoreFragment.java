package com.staples.mobile.cfa.store;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.staples.mobile.R;
import com.staples.mobile.common.access.Access;
import com.staples.mobile.common.access.channel.model.store.*;
import com.staples.mobile.common.access.easyopen.model.ApiError;

import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class StoreFragment extends Fragment implements Callback<StoreQuery>, GoogleMap.OnMarkerClickListener, AdapterView.OnItemClickListener{
    private static final String TAG = "StoreFragment";

    private static int FITSTORES = 5; // Number of stores to fit in initial view

    private MapView mapView;
    private GoogleMap googleMap;
    private ListView list;
    private StoreAdapter adapter;

    private BitmapDescriptor hotIcon;
    private BitmapDescriptor coldIcon;
    private Marker hotMarker;

    private double centerLat = 42.3672799; // Velocity lab
    private double centerLng = -71.0900776;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        View view;

        // Supports Google Play Services?
        if (GooglePlayServicesUtil.isGooglePlayServicesAvailable(getActivity())==0) {
            view = inflater.inflate(R.layout.store_fragment_map, container, false);
            mapView = (MapView) view.findViewById(R.id.map);
            mapView.onCreate(bundle);

            googleMap = mapView.getMap();
            googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            googleMap.setMyLocationEnabled(true);
            googleMap.setOnMarkerClickListener(this);

            MapsInitializer.initialize(getActivity());
            CameraUpdate update = CameraUpdateFactory.newLatLng(new LatLng(centerLat, centerLng));
            googleMap.moveCamera(update);
        }

        // No Google Play Services
        else {
            view = inflater.inflate(R.layout.store_fragment_nomap, container, false);
        }

        list = (ListView) view.findViewById(R.id.list);
        adapter = new StoreAdapter(getActivity());
        list.setAdapter(adapter);
        list.setOnItemClickListener(this);

        Access.getInstance().getChannelApi().storeLocations("02139", this);
        return (view);
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

    private String reformatNumber(String number) {
        if (number==null) return(null);
        number = number.trim();

        if (number.matches("[0-9]{10}")) {
            return("("+number.substring(0, 3)+") "+
                       number.substring(3, 6)+"-"+
                       number.substring(6, 10));
        }

        Log.d(TAG, "reformatNumber ->"+number+"<-");
        return(number);
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
       item.distance = storeData.getDis();

        // Get store address
        StoreAddress storeAddress = obj.getStoreAddress();
        if (storeAddress!=null) {
            item.streetAddress1 = storeAddress.getAddressLine1();
            item.streetAddress2 = storeAddress.getAddressLine2();
            item.city = storeAddress.getCity();
            item.state = storeAddress.getState();
            item.country = storeAddress.getCountry();
            item.zipcode = storeAddress.getZip();
            item.phoneNumber = reformatNumber(storeAddress.getPhoneNumber());
            item.faxNumber = reformatNumber(storeAddress.getFaxNumber());
        }

        // Get store hours
        List<StoreHours> list = obj.getStoreHours();
        for(StoreHours hours : list) {
            TimeSpan span = TimeSpan.parse(hours.getDayName(), hours.getHours());
            if (span!=null)
                item.addTimeSpan(span);
        }

        adapter.addStore(item);
        return(item);
    }

    private void addMarkers() {
        int n = adapter.getCount();
        if (n<=0) return;

        // Create icons
        hotIcon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED);
        coldIcon = BitmapDescriptorFactory.fromResource(R.drawable.store);

        // Add hot marker
        StoreItem item = adapter.getItem(0);
        MarkerOptions options = new MarkerOptions();
        options.icon(hotIcon);
        options.anchor(0.5f, 1.0f);
        options.position(item.position);
        item.marker = googleMap.addMarker(options);
        hotMarker = item.marker;

        // Add cold markers
        for(int i = 1; i < n; i++) {
            item = adapter.getItem(i);
            options = new MarkerOptions();
            options.icon(coldIcon);
            options.anchor(0.5f, 0.5f);
            options.position(item.position);
            item.marker = googleMap.addMarker(options);
        }
    }

    private void scaleMap() {
        // Set maximum zoom
        double deltaLat = 0.02;
        double deltaLng = 0.02 / Math.cos(Math.PI / 180.0 * centerLat);

        // Get bounds of first N stores
        int n = Math.min(adapter.getCount(), FITSTORES);
        for(int i=0;i<n;i++) {
            StoreItem item = adapter.getItem(i);
            deltaLat = Math.max(deltaLat, Math.abs(item.position.latitude-centerLat));
            deltaLng = Math.max(deltaLng, Math.abs(item.position.longitude-centerLng));
        }

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
    public void success(StoreQuery storeQuery, Response response) {
        Activity activity = getActivity();
        if (activity==null) return;

        if (storeQuery==null) return;
        List<StoreData> storeDatas = storeQuery.getStoreData();
        if (storeDatas==null) return;

        // Add stores
        for(StoreData storeData : storeDatas) {
            addStore(storeData);
        }

        // Set markers
        if (googleMap!=null) {
            addMarkers();
            scaleMap();
            adapter.setSingleMode(true);
            adapter.setSingleIndex(0);
        }

        adapter.notifyDataSetChanged();
    }

    @Override
    public void failure(RetrofitError retrofitError) {
        Activity activity = getActivity();
        if (activity==null) return;

        String msg = ApiError.getErrorMessage(retrofitError);
        Toast.makeText(activity, msg, Toast.LENGTH_LONG).show();
        Log.d(TAG, msg);
    }

    // Markers and list items

    private void selectMarker(Marker marker) {
        if (hotMarker!=null) {
            hotMarker.setIcon(coldIcon);
            hotMarker.setAnchor(0.5f, 0.5f);
        }

        marker.setIcon(hotIcon);
        marker.setAnchor(0.5f, 1.0f);
        hotMarker = marker;
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        selectMarker(marker);
        if (adapter.isSingleMode()) {
            int index = adapter.findPositionByMarker(hotMarker);
            if (index>=0) {
                adapter.setSingleIndex(index);
                adapter.notifyDataSetChanged();
            }
        }
        return(false);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        // List of stores with no map available
        if (mapView==null) {
        }

        // Map with single store
        else if (adapter.isSingleMode()) {
            mapView.setVisibility(View.GONE);
            adapter.setSingleMode(false);
            adapter.notifyDataSetChanged();
            list.smoothScrollToPosition(adapter.getSingleIndex());
        }

        // List of stores with map available
        else {
            mapView.setVisibility(View.VISIBLE);
            adapter.setSingleMode(true);
            adapter.setSingleIndex(position);
            StoreItem item = adapter.getItem(position);
            selectMarker(item.marker);
            CameraUpdate update = CameraUpdateFactory.newLatLng(item.position);
            googleMap.moveCamera(update);
            adapter.notifyDataSetChanged();
        }
    }
}
