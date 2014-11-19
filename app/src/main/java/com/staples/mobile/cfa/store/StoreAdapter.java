package com.staples.mobile.cfa.store;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.staples.mobile.R;

import java.util.ArrayList;

public class StoreAdapter extends BaseAdapter {
    private LayoutInflater inflater;
    private ArrayList<StoreItem> array;
    private boolean singleMode;
    private int singleIndex;

    public StoreAdapter(Context context) {
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        array = new ArrayList<StoreItem>();
    }

    // Items

    @Override
    public int getCount() {
        if (singleMode) return(1);
        else return(array.size());
    }

    @Override
    public long getItemId(int position) {
        if (singleMode) return(singleIndex);
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

        String text = item.city+"\n"+ item.streetAddress1 + "\n" + "Store #" + item.storeNumber;
        ((TextView) view.findViewById(R.id.title)).setText(text);

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

    public int findPositionByLatLng(LatLng location) {
        int n = array.size();
        for(int i=0;i<n;i++) {
            StoreItem item = array.get(i);
            if (item.position.latitude==location.latitude &&
                item.position.longitude==location.longitude)
                return(i);
        }
        return(-1);
    }
}
