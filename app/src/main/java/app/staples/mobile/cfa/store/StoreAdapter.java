package app.staples.mobile.cfa.store;

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

import java.text.DecimalFormat;
import java.util.ArrayList;

import app.staples.R;

public class StoreAdapter extends RecyclerView.Adapter<StoreAdapter.ViewHolder> {
    private static final String TAG = StoreAdapter.class.getSimpleName();

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView city;
        private TextView street;
        private TextView weeklyAd;
        private TextView distance;
        private TextView openTime;
        private View callStore;
        private View directions;
        // store detail views
        private View storeDetailLayout;
        private TextView storeNumber;
        private TextView storeSchedule;
        private TextView storeFeatures;

        private ViewHolder(View view) {
            super(view);
            city = (TextView) view.findViewById(R.id.city);
            street = (TextView) view.findViewById(R.id.street);
            weeklyAd = (TextView) view.findViewById(R.id.weekly_ad_link);
            distance = (TextView) view.findViewById(R.id.distance);
            openTime = (TextView) view.findViewById(R.id.open_time);
            callStore = view.findViewById(R.id.call_store);
            directions = view.findViewById(R.id.directions);
            // store detail
            storeDetailLayout = view.findViewById(R.id.store_detail_layout);
            storeNumber = (TextView) view.findViewById(R.id.store_number);
            storeSchedule = (TextView) view.findViewById(R.id.store_schedule);
            storeFeatures = (TextView) view.findViewById(R.id.store_features);
        }
    }

    private LayoutInflater inflater;
    private ArrayList<StoreItem> array;
    private View.OnClickListener listener;
    private DecimalFormat mileFormat;

    public StoreAdapter(Context context) {
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        array = new ArrayList<StoreItem>();
        mileFormat = new DecimalFormat("0.0 mi");
    }

    public void setOnClickListener(View.OnClickListener listener) {
        this.listener = listener;
    }

    @Override
    public int getItemCount() {
        return(array.size());
    }

    @Override
    public long getItemId(int position) {
        return(position);
    }

    public StoreItem getItem(int position) {
        return(array.get(position));
    }

    public void addItem(StoreItem item) {
        array.add(item);
    }

    public void clear() {
        array.clear();
    }

    // Views

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int type) {
        View view = inflater.inflate(R.layout.store_item, parent, false);
        ViewHolder vh = new ViewHolder(view);

        // Set onClickListeners
        vh.itemView.setOnClickListener(listener);
        vh.callStore.setOnClickListener(listener);
        vh.directions.setOnClickListener(listener);
        vh.weeklyAd.setOnClickListener(listener);
        return(vh);
    }

    @Override
    public void onBindViewHolder(ViewHolder vh, int position) {
        StoreItem item = array.get(position);
        onBindViewHolder(vh, item, false);
    }

    public void onBindViewHolder(ViewHolder vh, StoreItem item, boolean fullDetail) {
        // Set tag for onClickListeners
        vh.itemView.setTag(item);
        vh.callStore.setTag(item);
        vh.directions.setTag(item);
        vh.weeklyAd.setTag(item);

        // Set content and null checks to eliminate crashes
        if(item.city !=null){
            vh.city.setText(item.city);
        }
        if(item.streetAddress1 != null){
            vh.street.setText(item.streetAddress1);
        }
        vh.distance.setText(mileFormat.format(item.distance));

        vh.openTime.setText(TimeSpan.formatStatus(vh.itemView.getContext(), item.getSpans(), System.currentTimeMillis()));

        // Set detail content
        if (fullDetail) {
            vh.storeDetailLayout.setVisibility(View.VISIBLE);
            vh.storeNumber.setText("#" + item.storeNumber);
            Context context = vh.itemView.getContext();
            String schedule = TimeSpan.formatSchedule(context, item.getSpans());
            SpannableString span = new SpannableString(schedule);
            int x = context.getResources().getDimensionPixelOffset(R.dimen.store_schedule_tabstop);
            span.setSpan(new TabStopSpan.Standard(x), 0, span.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            vh.storeSchedule.setText(span);
            vh.storeFeatures.setText(item.storeFeatures);
        } else {
            vh.storeDetailLayout.setVisibility(View.GONE);
        }
    }

    // Map marker finder

    public int findPositionByMarker(Marker marker) {
        LatLng position = marker.getPosition();
        double latitude = position.latitude;
        double longitude = position.longitude;
        int n = array.size();
        for(int i=0;i<n;i++) {
            StoreItem item = array.get(i);
            if (item.latitude==latitude &&
                item.longitude==longitude)
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
