package com.staples.mobile.cfa.store;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.staples.mobile.cfa.R;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class StoreAdapter extends RecyclerView.Adapter<StoreAdapter.ViewHolder> {
    private static final String TAG = "StoreAdapter";

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView city;
        private TextView street;
        private TextView phone;
        private TextView distance;
        private TextView openTime;
        private View callStore;
        private View directions;
        // store detail views
        private View storeDetailLayout;
        private TextView phone2;
        private View callStore2;
        private TextView storeNumber;
        private TextView storeDays;
        private TextView storeHours;
        private TextView storeFeatures;

        private ViewHolder(View view) {
            super(view);
            city = (TextView) view.findViewById(R.id.city);
            street = (TextView) view.findViewById(R.id.street);
            phone = (TextView) view.findViewById(R.id.phone);
            distance = (TextView) view.findViewById(R.id.distance);
            openTime = (TextView) view.findViewById(R.id.open_time);
            callStore = view.findViewById(R.id.call_store);
            directions = view.findViewById(R.id.directions);
            // store detail
            storeDetailLayout = view.findViewById(R.id.store_detail_layout);
            storeNumber = (TextView) view.findViewById(R.id.store_number);
            phone2 = (TextView) view.findViewById(R.id.phone2);
            callStore2 = view.findViewById(R.id.call_store2);
            storeDays = (TextView) view.findViewById(R.id.store_days);
            storeHours = (TextView) view.findViewById(R.id.store_hours);
            storeFeatures = (TextView) view.findViewById(R.id.store_features);
        }
    }

    private Activity activity;
    private LayoutInflater inflater;
    private ArrayList<StoreItem> array;
    private View.OnClickListener listener;

    private boolean fullStoreDetail;
    private boolean singleMode;
    private int singleIndex;
    private DecimalFormat mileFormat;
    private SimpleDateFormat dateFormat;

    public StoreAdapter(Context context) {
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        array = new ArrayList<StoreItem>();
        mileFormat = new DecimalFormat("0.0 mi");
        dateFormat = new SimpleDateFormat("EEE h:mma");
    }

    public void setOnClickListener(View.OnClickListener listener) {
        this.listener = listener;
    }

    // Shadowed standard methods

    @Override
    public int getItemCount() {
        if (singleMode) {
            if (singleIndex>=array.size()) return(0);
            return(1);
        }
        else return(array.size());
    }

    @Override
    public long getItemId(int position) {
        if (singleMode) return(0);
        else return(position);
    }

    // Full backing array methods

    public int getBackingCount() {
        return(array.size());
    }

    public StoreItem getBackingItem(int position) {
        return(array.get(position));
    }

    public void clear() {
        array.clear();
        singleMode = false;
        singleIndex = 0;
    }

    public void addStore(StoreItem item) {
        array.add(item);
    }

    // Views

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int type) {
        View view = inflater.inflate(R.layout.store_item, parent, false);
        ViewHolder bvh = new ViewHolder(view);

        // Set onClickListeners
        bvh.itemView.setOnClickListener(listener);
        bvh.callStore.setOnClickListener(listener);
        bvh.callStore2.setOnClickListener(listener);
        bvh.directions.setOnClickListener(listener);
        return(bvh);
    }

    @Override
    public void onBindViewHolder(ViewHolder vh, int position) {
        int index = singleMode ? singleIndex : position;
        StoreItem item = array.get(index);

        // Set tag for onClickListeners
        vh.itemView.setTag(item);
        vh.callStore.setTag(item);
        vh.callStore2.setTag(item);
        vh.directions.setTag(item);

        // show or hide content depending on whether full store details are to be displayed
        if (isFullStoreDetail()) {
            vh.phone.setVisibility(View.GONE);
            vh.callStore.setVisibility(View.GONE);
            vh.phone2.setVisibility(View.VISIBLE);
            vh.callStore2.setVisibility(View.VISIBLE);
            vh.storeNumber.setVisibility(View.VISIBLE);
            vh.storeDetailLayout.setVisibility(View.VISIBLE);
        } else {
            // else summary display
            vh.phone.setVisibility(View.VISIBLE);
            vh.callStore.setVisibility(View.VISIBLE);
            vh.phone2.setVisibility(View.GONE);
            vh.callStore2.setVisibility(View.GONE);
            vh.storeNumber.setVisibility(View.GONE);
            vh.storeDetailLayout.setVisibility(View.GONE);
        }

        // Set content
        vh.city.setText(item.city);
        vh.street.setText(item.streetAddress1);
        vh.phone.setText(item.phoneNumber);
        vh.distance.setText(mileFormat.format(item.distance));
        String openTime = item.formatHours(System.currentTimeMillis(), dateFormat);
        vh.openTime.setText(openTime);

        // Set detail content
        if (isFullStoreDetail()) {
            vh.phone2.setText(item.phoneNumber);
            vh.storeNumber.setText("Store # " + item.storeNumber);
            boolean condensedHours = item.areWeekdayHoursIdentical();
            vh.storeDays.setText(item.getStoreDaysText(condensedHours));
            vh.storeHours.setText(item.getStoreHoursText(condensedHours));
            vh.storeFeatures.setText(item.storeFeatures);
        }
    }

    // Mode getters & setters

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

    public boolean isFullStoreDetail() {
        return fullStoreDetail;
    }

    public void setFullStoreDetail(boolean fullStoreDetail) {
        this.fullStoreDetail = fullStoreDetail;
    }

    // Map marker finder

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
