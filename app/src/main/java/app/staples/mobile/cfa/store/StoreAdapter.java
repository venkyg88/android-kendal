package app.staples.mobile.cfa.store;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.TabStopSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import app.staples.mobile.cfa.R;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class StoreAdapter extends RecyclerView.Adapter<StoreAdapter.ViewHolder> {
    private static final String TAG = StoreAdapter.class.getSimpleName();

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView city;
        private TextView street;
        private TextView phone;
        private TextView weeklyAd;
        private TextView weeklyAd2;
        private TextView distance;
        private TextView openTime;
        private View callStore;
        private View directions;
        // store detail views
        private View storeDetailLayout;
        private TextView phone2;
        private View callStore2;
        private TextView storeNumber;
        private TextView storeSchedule;
        private TextView storeFeatures;

        private ViewHolder(View view) {
            super(view);
            city = (TextView) view.findViewById(R.id.city);
            street = (TextView) view.findViewById(R.id.street);
            phone = (TextView) view.findViewById(R.id.phone);
            weeklyAd = (TextView) view.findViewById(R.id.weekly_ad_link);
            weeklyAd2 = (TextView) view.findViewById(R.id.weekly_ad_link2);
            distance = (TextView) view.findViewById(R.id.distance);
            openTime = (TextView) view.findViewById(R.id.open_time);
            callStore = view.findViewById(R.id.call_store);
            directions = view.findViewById(R.id.directions);
            // store detail
            storeDetailLayout = view.findViewById(R.id.store_detail_layout);
            storeNumber = (TextView) view.findViewById(R.id.store_number);
            phone2 = (TextView) view.findViewById(R.id.phone2);
            callStore2 = view.findViewById(R.id.call_store2);
            storeSchedule = (TextView) view.findViewById(R.id.store_schedule);
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

    public StoreAdapter(Context context) {
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        array = new ArrayList<StoreItem>();
        mileFormat = new DecimalFormat("0.0 mi");
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
        bvh.weeklyAd.setOnClickListener(listener);
        bvh.weeklyAd2.setOnClickListener(listener);
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
        vh.weeklyAd.setTag(item);
        vh.weeklyAd2.setTag(item);

        // show or hide content depending on whether full store details are to be displayed
        if (isFullStoreDetail()) {
            vh.phone.setVisibility(View.GONE);
            vh.callStore.setVisibility(View.GONE);
            vh.weeklyAd.setVisibility(View.GONE);
            vh.phone2.setVisibility(View.VISIBLE);
            vh.callStore2.setVisibility(View.VISIBLE);
            vh.weeklyAd2.setVisibility(View.VISIBLE);
            vh.storeNumber.setVisibility(View.VISIBLE);
            vh.storeDetailLayout.setVisibility(View.VISIBLE);
        } else {
            // else summary display
            vh.phone.setVisibility(View.VISIBLE);
            vh.callStore.setVisibility(View.VISIBLE);
            vh.weeklyAd.setVisibility(View.VISIBLE);
            vh.phone2.setVisibility(View.GONE);
            vh.callStore2.setVisibility(View.GONE);
            vh.weeklyAd2.setVisibility(View.GONE);
            vh.storeNumber.setVisibility(View.GONE);
            vh.storeDetailLayout.setVisibility(View.GONE);
        }

        // Set content
        vh.city.setText(item.city);
        vh.street.setText(item.streetAddress1);
        vh.phone.setText(item.phoneNumber);
        vh.distance.setText(mileFormat.format(item.distance));
        vh.openTime.setText(TimeSpan.formatStatus(vh.itemView.getContext(), item.getSpans(), System.currentTimeMillis()));

        // Set detail content
        if (isFullStoreDetail()) {
            vh.phone2.setText(item.phoneNumber);
            vh.storeNumber.setText("Store # " + item.storeNumber);
            Context context = vh.itemView.getContext();
            String schedule = TimeSpan.formatSchedule(context, item.getSpans());
            SpannableString span = new SpannableString(schedule);
            int x = context.getResources().getDimensionPixelOffset(R.dimen.store_schedule_tabstop);
            span.setSpan(new TabStopSpan.Standard(x), 0, span.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            vh.storeSchedule.setText(span);
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

    public int findPositionByItem(StoreItem storeItem) {
        int n = array.size();
        for(int i=0;i<n;i++) {
            StoreItem item = array.get(i);
            if (item == storeItem)
                return (i);
        }
        return(-1);
    }
}
