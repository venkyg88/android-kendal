package app.staples.mobile.cfa.store;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.crittercism.app.Crittercism;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.staples.mobile.common.access.Access;
import com.staples.mobile.common.access.channel.model.store.Obj;
import com.staples.mobile.common.access.channel.model.store.StoreAddress;
import com.staples.mobile.common.access.channel.model.store.StoreData;
import com.staples.mobile.common.access.channel.model.store.StoreFeature;
import com.staples.mobile.common.access.channel.model.store.StoreHours;
import com.staples.mobile.common.access.channel.model.store.StoreQuery;
import com.staples.mobile.common.access.easyopen.model.ApiError;
import com.staples.mobile.common.analytics.Tracker;

import java.util.List;
import java.util.Locale;

import app.staples.R;
import app.staples.mobile.cfa.MainActivity;
import app.staples.mobile.cfa.location.LocationFinder;
import app.staples.mobile.cfa.widget.ActionBar;
import app.staples.mobile.cfa.widget.PlaceFieldView;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class StoreFragment extends Fragment implements Callback<StoreQuery>,
        OnMapReadyCallback, GoogleMap.OnMarkerClickListener, GoogleMap.OnMapClickListener, View.OnClickListener, PlaceFieldView.OnPlaceDoneListener {
    private static final String TAG = StoreFragment.class.getSimpleName();

    private enum Mode {
        SINGLE, DETAILS, MULTIPLE;
    }

    private static int FITSTORES = 5; // Number of stores to fit in initial view
    private static double EARTHRADIUS = 6371.0; // kilometers
    private static double minViewAngle = 360.0/(2.0*Math.PI*EARTHRADIUS) * 5.0; // 5 km
    private static double maxViewAngle = 360.0/(2.0*Math.PI*EARTHRADIUS) * 100.0; // 100 km

    private View mapLayout;
    private MapView mapView;
    private GoogleMap googleMap;
    private PlaceFieldView searchText;
    private RecyclerView list;
    private StoreAdapter adapter;

    private BitmapDescriptor hotIcon;
    private BitmapDescriptor coldIcon;
    private Marker hotMarker;

    private Location location;
    private StoreAdapter.ViewHolder singleVh;
    private Mode mode;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        adapter = new StoreAdapter(getActivity());
        gotoHere();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        Crittercism.leaveBreadcrumb("StoreFragment:onCreateView(): Displaying the Store screen.");
        ViewGroup view;

        // Supports Google Play Services?
        if (GooglePlayServicesUtil.isGooglePlayServicesAvailable(getActivity())==ConnectionResult.SUCCESS) {
            view = (ViewGroup) inflater.inflate(R.layout.store_fragment_map, container, false);
            view.setTag(this);

            mapLayout = view.findViewById(R.id.map_layout);
            mapView = (MapView) view.findViewById(R.id.map);
            mapView.onCreate(bundle);
            mapView.getMapAsync(this);
        }

        // No Google Play Services
        else {
            view = (ViewGroup) inflater.inflate(R.layout.store_fragment_nomap, container, false);
            view.setTag(this);
        }

        list = (RecyclerView) view.findViewById(R.id.list);
        list.setAdapter(adapter);
        list.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        adapter.setOnClickListener(this);

        singleVh = adapter.onCreateViewHolder(view, 0);
        view.addView(singleVh.itemView);

        searchText = (PlaceFieldView) view.findViewById(R.id.store_search);
        searchText.setHint(getHint());
        searchText.selectMode(true);
        searchText.setOnPlaceDoneListener(this);

        view.findViewById(R.id.goto_here).setOnClickListener(this);
        View optionButton = view.findViewById(R.id.view_list);
        optionButton.setOnClickListener(this);

        return (view);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;

        // Set options
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        googleMap.setMyLocationEnabled(true);
        UiSettings settings = googleMap.getUiSettings();
        settings.setMyLocationButtonEnabled(true);
        settings.setMapToolbarEnabled(false);

        // Set listeners
        googleMap.setOnMarkerClickListener(this);
        googleMap.setOnMapClickListener(this);

        // Create icons
        MapsInitializer.initialize(getActivity());
        hotIcon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED);
        coldIcon = BitmapDescriptorFactory.fromResource(R.drawable.ic_store_cold);

        applyState();
    }

    private void applyState() {
        if (adapter.getItemCount()>0 && googleMap!=null) {
            addMarkers();
            scaleMap();
            showSingle(adapter.getItem(0));
        }
    }

    private CharSequence getHint() {
        Resources res = getActivity().getResources();
        SpannableStringBuilder sb = new SpannableStringBuilder("   ");
        sb.append(res.getString(R.string.store_search_hint));
        Drawable icon = res.getDrawable(R.drawable.ic_search_black);
        int size = (int) (searchText.getTextSize()*1.25);
        icon.setBounds(0, 0, size, size);
        sb.setSpan(new ImageSpan(icon), 1, 2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return(sb);
    }

    @Override
    public void onResume() {
        int icon;
        super.onResume();
        if (mapView!=null) mapView.onResume();
        ActionBar.getInstance().setConfig(ActionBar.Config.STORE);
        Tracker.getInstance().trackStateForStoreFinder(); // Analytics
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

    // Map bounds and scaling

    private LatLngBounds makeBounds(double centerLat, double centerLng, double deltaLat, double deltaLng) {
        // Inflate for padding
        deltaLat *= 1.1;
        deltaLng *= 1.1;

        // Clip deltas to min and max
        double cos = Math.cos(Math.PI / 180.0 * centerLat);
        deltaLat = Math.max(deltaLat, minViewAngle);
        deltaLng = Math.max(deltaLng, minViewAngle/cos);
        deltaLat = Math.min(deltaLat, maxViewAngle);
        deltaLng = Math.min(deltaLng, maxViewAngle/cos);

        // Make bounds
        LatLng northeast = new LatLng(centerLat+deltaLat, centerLng+deltaLng);
        LatLng southwest = new LatLng(centerLat-deltaLat, centerLng-deltaLng);
        LatLngBounds bounds = new LatLngBounds(southwest, northeast);
        return(bounds);
    }

    private LatLngBounds getCenteredBounds() {
        double centerLat = location.getLatitude();
        double centerLng = location.getLongitude();
        double deltaLat = 0.0;
        double deltaLng = 0.0;

        // Get bounds of first N stores
        int n = Math.min(adapter.getItemCount(), FITSTORES);
        for(int i=0;i<n;i++) {
            StoreItem item = adapter.getItem(i);
            deltaLat = Math.max(deltaLat, Math.abs(item.latitude-centerLat));
            deltaLng = Math.max(deltaLng, Math.abs(item.longitude-centerLng));
        }

        LatLngBounds bounds = makeBounds(centerLat, centerLng, deltaLat, deltaLng);
        return(bounds);
    }

    private LatLngBounds getCollectionBounds() {
        double minLat = 90.0;
        double minLng = 180.0;
        double maxLat = -90.0;
        double maxLng = -180.0;

        // Get bounds of first N stores
        int n = Math.min(adapter.getItemCount(), FITSTORES);
        for(int i=0;i<n;i++) {
            StoreItem item = adapter.getItem(i);
            minLat = Math.min(minLat, item.latitude);
            minLng = Math.min(minLng, item.longitude);
            maxLat = Math.max(maxLat, item.latitude);
            maxLng = Math.max(maxLng, item.longitude);
        }

        LatLngBounds bounds = makeBounds((minLat+maxLat)/2.0, (minLng+maxLng)/2.0,
                                         (maxLat-minLat)/2.0, (maxLng-minLng)/2.0);
        return(bounds);
    }

    private void scaleMap() {
        LatLngBounds bounds;
        if (location==null) bounds = getCollectionBounds();
        else bounds = getCenteredBounds();

        // Get map dimensions
        int width = mapView.getWidth();
        int height = mapView.getHeight();

        // Zoom to bounds
        if (width>0 && height>0) {
            CameraUpdate update = CameraUpdateFactory.newLatLngBounds(bounds, width, height, 0);
            googleMap.moveCamera(update);
        }
    }

    // Retrofit callbacks & processing

    @Override
    public void onPlaceDone(PlaceFieldView.Place place) {
        // Hide keyboard
        MainActivity activity = (MainActivity) getActivity();
        activity.hideSoftKeyboard();

        // Get, then clear control
        String address = searchText.getText().toString();
        searchText.setText(null);

        if (place!=null) {
            if (place.postalCode!=null) {
                address = place.postalCode;
            } else if (place.city!=null && place.state!=null) {
                address = place.city+", "+place.state;
            }
        }

        location = null;
        Access.getInstance().getChannelApi(false).storeLocations(address, this);
    }

    private void gotoHere() {
        LocationFinder finder = LocationFinder.getInstance(getActivity());
        location = finder.getLocation();
        String postalCode = finder.getPostalCode();
        Access.getInstance().getChannelApi(false).storeLocations(postalCode, this);
    }

    @Override
    public void success(StoreQuery storeQuery, Response response) {
        Activity activity = getActivity();
        if (!(activity instanceof MainActivity)) return;

        int n = processStoreQuery(storeQuery);
        if (n>0) {
            applyState();
        } else {
            showFailureDialog();
        }
    }

    @Override
    public void failure(RetrofitError retrofitError) {
        Activity activity = getActivity();
        if (!(activity instanceof MainActivity)) return;

        showFailureDialog();
        String msg = ApiError.getErrorMessage(retrofitError);
        Log.d(TAG, msg);
    }

    private int processStoreQuery(StoreQuery storeQuery) {
        if (storeQuery==null) return(0);
        List<StoreData> storeDatas = storeQuery.getStoreData();
        if (storeDatas==null || storeDatas.size()==0) return(0);

        // Add stores
        adapter.clear();
        for(StoreData storeData : storeDatas) {
            StoreItem item = processStoreData(storeData);
            if (item!=null) {
                adapter.addItem(item);
            }
        }

        adapter.notifyDataSetChanged();
        return(adapter.getItemCount());
    }

    // This is also used by LocationFinder to get nearby store
    public static StoreItem processStoreData(StoreData storeData) {
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
        StoreItem item = new StoreItem();
        item.storeNumber = storeNumber;
        item.latitude = lat;
        item.longitude = lng;
        item.distance = storeData.getDis();

        // Store features
        StringBuilder featuresBuf = new StringBuilder();
        for (StoreFeature feature : obj.getStoreFeatures()) {
            if (!TextUtils.isEmpty(feature.getName())) {
                if (featuresBuf.length() > 0) {
                    featuresBuf.append("\n");
                }
                featuresBuf.append(feature.getName());
            }
        }
        item.storeFeatures = featuresBuf.toString();

        // Get store address
        StoreAddress storeAddress = obj.getStoreAddress();
        if (storeAddress!=null) {
            item.streetAddress1 = storeAddress.getAddressLine1();
            item.streetAddress2 = storeAddress.getAddressLine2();
            item.city = storeAddress.getCity();
            item.state = storeAddress.getState();
            item.country = storeAddress.getCountry();
            item.postalCode = storeAddress.getZip();
            item.phoneNumber = StoreItem.reformatPhoneFaxNumber(storeAddress.getPhoneNumber());
            item.faxNumber = StoreItem.reformatPhoneFaxNumber(storeAddress.getFaxNumber());
        }

        // Get store hours
        List<StoreHours> list = obj.getStoreHours();
        for(StoreHours hours : list) {
            TimeSpan span = TimeSpan.parse(hours.getDayName(), hours.getHours());
            if (span!=null)
                item.addTimeSpan(span);
        }
        return(item);
    }

    // Failure dialog

    private void showFailureDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.no_stores_title);
        builder.setMessage(R.string.no_stores_message);
        builder.setPositiveButton(R.string.no_stores_ok, null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    // Markers and list items

    private void addMarkers() {
        googleMap.clear();
        int n = adapter.getItemCount();
        for(int i=0;i<n;i++) {
            StoreItem item = adapter.getItem(i);
            MarkerOptions options = new MarkerOptions();
            options.position(new LatLng(item.latitude, item.longitude));
            if (i==0) {
                // Add hot marker
                options.icon(hotIcon);
                options.anchor(0.5f, 1.0f);
            } else {
                // Add cold marker
                options.icon(coldIcon);
                options.anchor(0.5f, 0.5f);
            }
            item.marker = googleMap.addMarker(options);
            if (i==0) {
                hotMarker = item.marker;
            }
        }
    }

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
        int index = adapter.findPositionByMarker(hotMarker);
        StoreItem item = adapter.getItem(index);
        showSingle(item);
        return(false);
    }

    @Override
    public void onMapClick(LatLng latLng) {
        if (mode==Mode.DETAILS) {
            showSingle(null);
        }
    }

    // Mode methods

    private void showSingle(StoreItem item) {
        if (item==null) {
            item = (StoreItem) singleVh.itemView.getTag();
        }

        if (mapLayout!=null) {
            mapLayout.setVisibility(View.VISIBLE);
        }
        list.setVisibility(View.GONE);
        singleVh.itemView.setVisibility(View.VISIBLE);
        adapter.onBindViewHolder(singleVh, item, false);

        mode = Mode.SINGLE;
    }

    private void showDetails(StoreItem item) {
        if (item==null) {
            item = (StoreItem) singleVh.itemView.getTag();
        }

        if (mapLayout!=null) {
            mapLayout.setVisibility(View.VISIBLE);
        }
        list.setVisibility(View.GONE);
        singleVh.itemView.setVisibility(View.VISIBLE);
        adapter.onBindViewHolder(singleVh, item, true);

        mode = Mode.DETAILS;
        Tracker.getInstance().trackStateForStoreDetail();
    }

    private void showMultiple() {
        if (mapLayout!=null) {
            mapLayout.setVisibility(View.GONE);
        }
        list.setVisibility(View.VISIBLE);
        singleVh.itemView.setVisibility(View.GONE);

        mode = Mode.MULTIPLE;
        Tracker.getInstance().trackStateForStoreResults();
    }

    // Intent actions

    private boolean dialStorePhone(StoreItem item) {
        if (item==null) return(false);
        String phone = item.phoneNumber;
        if (phone==null || phone.isEmpty()) return(false);
        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phone));
        try {
            startActivity(intent);
            return(true);
        } catch(ActivityNotFoundException e) {
            Crittercism.logHandledException(e);
            ((MainActivity) getActivity()).showErrorDialog(R.string.store_no_phone);
            return(false);
        }
    }

    private boolean getStoreDirections(StoreItem item) {
        if (item==null) return(false);
        String query = String.format(Locale.ENGLISH, "http://maps.google.com/maps?&daddr=%f,%f(Staples%%20%%23%s)",
                                     item.latitude, item.longitude, item.storeNumber);
        Uri uri = Uri.parse(query);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
        try {
            startActivity(intent);
            return(true);
        } catch(ActivityNotFoundException e1) {
            Crittercism.logHandledException(e1);
            intent = new Intent(Intent.ACTION_VIEW, uri);
            try {
                startActivity(intent);
                return(true);
            } catch(ActivityNotFoundException e2) {
                Crittercism.logHandledException(e2);
                ((MainActivity) getActivity()).showErrorDialog(R.string.store_no_maps);
                return (false);
            }
        }
    }

    @Override
    public void onClick(View view) {
        Object obj;
        switch(view.getId()) {
            case R.id.view_list:
                showMultiple();
                break;
            case R.id.goto_here:
                gotoHere();
                break;
            case R.id.store_item:
                obj = view.getTag();
                if (obj instanceof StoreItem) {
                    StoreItem storeItem = (StoreItem) obj;
                    if (mode==Mode.MULTIPLE) {
                        // TODO This needs to be thought through, RP
                        onMarkerClick(storeItem.marker);
                        location = new Location("Nowhere");
                        location.setLatitude(storeItem.latitude);
                        location.setLongitude(storeItem.longitude);
                        scaleMap();
                    }
                    showDetails(storeItem);
                }
                break;
            case R.id.call_store:
                obj = view.getTag();
                if (obj instanceof StoreItem) {
                    StoreItem storeItem = (StoreItem) obj;
                    Tracker.getInstance().trackActionForCallStore(storeItem.storeNumber); // analytics
                    dialStorePhone(storeItem);
                }
                break;
            case R.id.weekly_ad_link:
                obj = view.getTag();
                if (obj instanceof StoreItem) {
                    StoreItem storeItem = (StoreItem) obj;
                    Tracker.getInstance().trackActionForStoreLocatorWeeklyAd(storeItem.storeNumber);
                    ((MainActivity)getActivity()).selectWeeklyAd(storeItem.storeNumber);
                }
                break;
            case R.id.directions:
                obj = view.getTag();
                if (obj instanceof StoreItem) {
                    StoreItem storeItem = (StoreItem) obj;
                    Tracker.getInstance().trackActionForStoreDirections(storeItem.storeNumber);
                    getStoreDirections(storeItem);
                }
                break;
        }
    }
}
