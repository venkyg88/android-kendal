package app.staples.mobile.cfa.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListPopupWindow;
import android.widget.TextView;

import com.staples.mobile.common.access.Access;
import com.staples.mobile.common.access.google.api.GoogleApi;
import com.staples.mobile.common.access.google.model.places.AddressComponent;
import com.staples.mobile.common.access.google.model.places.AutoComplete;
import com.staples.mobile.common.access.google.model.places.Details;
import com.staples.mobile.common.access.google.model.places.Prediction;
import com.staples.mobile.common.access.google.model.places.Result;

import java.lang.reflect.Type;
import java.util.List;

import app.staples.R;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class PlaceFieldView extends DualHintEdit implements TextWatcher, TextView.OnEditorActionListener, AdapterView.OnItemClickListener, Callback {
    private static final String TAG = PlaceFieldView.class.getSimpleName();

    private static final int KEYDELAY = 250; // milliseconds

    public interface PlaceFieldWatcher {
        void onPlaceFieldStart();
        void onPlaceFieldDone(Place place);
    }

    public static class Place {
        public String streetAddress;
        public String city;
        public String state;
        public String postalCode;
    }

    private static class Item {
        private String title;
        private String placeId;

        private Item(String title, String placeId) {
            this.title = title;
            this.placeId = placeId;
        }

        @Override
        public String toString() {
            return(title);
        }
    }

    private ListPopupWindow popup;
    private ArrayAdapter<Item> adapter;
    private boolean autoMode;
    private PlaceFieldWatcher watcher;
    private StartSuggest startSuggest;
    private String searchTypes;
    private String manualEntry;

    public PlaceFieldView(Context context) {
        super(context);
        init(context, null);
    }

    public PlaceFieldView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public PlaceFieldView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        // Default attributes
        int width = ViewGroup.LayoutParams.WRAP_CONTENT;
        int offset = 0;

        // Get styled attributes
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.PlacesFieldView);
        int n = a.getIndexCount();
        for(int i=0;i<n;i++) {
            int index = a.getIndex(i);
            switch(index) {
                case R.styleable.PlacesFieldView_searchTypes:
                    searchTypes = a.getString(index);
                    break;
                case R.styleable.PlacesFieldView_manualPrompt:
                    manualEntry = a.getString(index);
                    break;
                case R.styleable.PlacesFieldView_android_dropDownWidth:
                    width = a.getLayoutDimension(index, width);
                    break;
                case R.styleable.PlacesFieldView_android_dropDownHorizontalOffset:
                    offset = a.getDimensionPixelSize(index, offset);
                    break;
            }
        }
        a.recycle();

        // Add popup & adapter
        popup = new ListPopupWindow(context);
        popup.setAnchorView(this);
        popup.setWidth(width);
        popup.setHorizontalOffset(offset);
        adapter = new ArrayAdapter<Item>(context, R.layout.place_suggest_item);
        popup.setAdapter(adapter);

        // Tasks
        startSuggest = new StartSuggest();

        // Set listeners
        setOnEditorActionListener(this);
        popup.setOnItemClickListener(this);
    }

    public void selectMode(boolean autoMode) {
        if (autoMode) addTextChangedListener(this);
        else removeTextChangedListener(this);
        this.autoMode = autoMode;
    }

    public void setPlaceFieldWatcher(PlaceFieldWatcher watcher) {
        this.watcher = watcher;
    }

    // Listeners

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        removeCallbacks(startSuggest);
        if (autoMode) {
            String address = s.toString().trim();
            if (address.length() >= 3)
                postDelayed(startSuggest, KEYDELAY);
        }
        if (watcher != null) watcher.onPlaceFieldStart();
    }

    @Override
    public void afterTextChanged(Editable s) {
    }

    @Override
    public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
        switch(actionId) {
            case EditorInfo.IME_ACTION_NEXT:
                activateItem(null);
                return(true);
            case EditorInfo.IME_NULL:
                if (event.getKeyCode()==KeyEvent.KEYCODE_ENTER &&
                        event.getAction()==KeyEvent.ACTION_DOWN) {
                    activateItem(null);
                }
                return(true);
        }
        return(false);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Item item = adapter.getItem(position);
        activateItem(item);
    }

    private void activateItem(Item item) {
        if (autoMode) { // this method currently gets called even when autoMode turned off
            popup.dismiss();
            if (item == null && adapter.getCount() > 0) item = adapter.getItem(0);
            if (item == null || item.placeId == null) {
                if (watcher != null) watcher.onPlaceFieldDone(null);
            } else {
                GoogleApi googleApi = Access.getInstance().getGoogleApi();
                googleApi.getPlaceDetails(item.placeId, this);
            }
        }
    }

    private class StartSuggest implements Runnable {
        @Override
        public void run() {
            String address = getText().toString();
            GoogleApi googleApi = Access.getInstance().getGoogleApi();
            googleApi.getPlaceAutoComplete(searchTypes, "country:us", address, PlaceFieldView.this);
        }
    }

    @Override
    public void success(Object obj, Response response) {
        if (obj instanceof AutoComplete) {
            AutoComplete autoComplete = (AutoComplete) obj;
            adapter.clear();
            List<Prediction> predictions = autoComplete.getPredictions();
            if (predictions == null || predictions.size() == 0) {
                adapter.notifyDataSetInvalidated();
                return;
            }

            for(Prediction prediction : predictions) {
                Item item = new Item(prediction.getDescription(), prediction.getPlaceId());
                adapter.add(item);
            }
            if (manualEntry!=null) {
                Item item = new Item(manualEntry, null);
                adapter.add(item);
            }
            adapter.notifyDataSetChanged();
            popup.show();
        }

        else if (obj instanceof Details) {
            Place place = parseDetails((Details) obj);
            if (place!=null && watcher !=null) {
                watcher.onPlaceFieldDone(place);
            }
        }
    }

    @Override
    public void failure(RetrofitError retrofitError) {
        Type type = retrofitError.getSuccessType();

        if (type==AutoComplete.class) {
            adapter.clear();
            adapter.notifyDataSetInvalidated();
        }

        else if (type==Details.class) {
        }
    }

    private static String joinStrings(String a, String divider, String b) {
        if (a!=null && b!=null) return(a+divider+b);
        if (a!=null) return(a);
        return(b);
    }

    private static Place parseDetails(Details details) {
        Result result = details.getResult();
        if (result==null) return(null);
        List<AddressComponent> components = result.getAddressComponents();
        if (components==null) return(null);

        Place place = new Place();
        String streetNumber = null;
        String streetName = null;
        String postalCode = null;
        String suffix = null;

        for(AddressComponent component : components) {
            List<String> types = component.getTypes();
            if (types.contains("street_number")) streetNumber = component.getShortName();
            if (types.contains("route")) streetName = component.getShortName();
            if (types.contains("sublocality") || types.contains("locality")) place.city = component.getLongName();
            if (types.contains("administrative_area_level_1")) place.state = component.getShortName();
            if (types.contains("postal_code")) postalCode = component.getShortName();
            if (types.contains("postal_code_suffix")) suffix = component.getShortName();
        }

        place.streetAddress = joinStrings(streetNumber, " ", streetName);
        place.postalCode = joinStrings(postalCode, "-", suffix);
        return(place);
    }
}
