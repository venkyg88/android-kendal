package com.staples.mobile.cfa.store;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.staples.mobile.R;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class StoreAdapter extends BaseAdapter {
    private static final String TAG = "StoreAdapter";

    private LayoutInflater inflater;
    private ArrayList<StoreItem> array;
    private boolean singleMode;
    private int singleIndex;
    private DecimalFormat mileFormat;

    public StoreAdapter(Context context) {
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        array = new ArrayList<StoreItem>();
        mileFormat = new DecimalFormat("0.0 mi");
    }

    // Items

    @Override
    public int getCount() {
        if (singleMode) return(1);
        else return(array.size());
    }

    @Override
    public long getItemId(int position) {
        if (singleMode) return(0);
        else return(position);
    }

    @Override
    public StoreItem getItem(int position) {
        if (singleMode) return(array.get(singleIndex));
        else return(array.get(position));
    }

    // Views

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        StoreItem item = getItem(position);

        if (view==null)
            view = inflater.inflate(R.layout.store_item, parent, false);

        ((TextView) view.findViewById(R.id.city)).setText(item.city);
        ((TextView) view.findViewById(R.id.street)).setText(item.streetAddress1);
        ((TextView) view.findViewById(R.id.phone)).setText(item.phoneNumber);
        ((TextView) view.findViewById(R.id.distance)).setText(mileFormat.format(item.distance));
        ((TextView) view.findViewById(R.id.opentime)).setText("11:00 PM");

        return(view);
    }

    // Getters & setters

    public int getSingleIndex() {
        return(singleIndex);
    }

    public void setSingleIndex(int singleIndex) {
        this.singleIndex = singleIndex;
    }

    public boolean isSingleMode() {
        return(singleMode);
    }

    public void setSingleMode(boolean singleMode) {
        this.singleMode = singleMode;
    }

    public void addStore(StoreItem item) {
        array.add(item);
    }

    public int findPositionByMarker(Marker marker) {
        LatLng position = marker.getPosition();
        double latitude = position.latitude;
        double longitude = position.longitude;
        int n = array.size();
        for(int i=0;i<n;i++) {
            StoreItem item = array.get(i);
            if (item.position.latitude==latitude &&
                item.position.longitude==longitude)
                return (i);
        }
        return(-1);
    }
}
