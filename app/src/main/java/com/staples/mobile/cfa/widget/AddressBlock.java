package com.staples.mobile.cfa.widget;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.staples.mobile.cfa.R;
import com.staples.mobile.cfa.profile.PlacesArrayAdapter;
import com.staples.mobile.cfa.profile.UsState;
import com.staples.mobile.common.access.easyopen.model.cart.ShippingAddress;
import com.staples.mobile.common.access.easyopen.model.member.Address;
import com.staples.mobile.common.access.easyopen.model.member.UpdateAddress;

public class AddressBlock extends LinearLayout implements TextView.OnEditorActionListener, AdapterView.OnItemClickListener {
    public static final String TAG = "AddressBlock";

    private static final int[] addressFields = {R.id.firstName, R.id.lastName, R.id.phoneNumber, R.id.emailAddr, R.id.addressACTV,
            R.id.addressET, R.id.apartment, R.id.city, R.id.state, R.id.zipCode};
    private static final int[] manualFields = {R.id.addressET, R.id.apartment, R.id.city, R.id.state, R.id.zipCode};

    public interface OnDoneListener {
        public void onDone(AddressBlock addressBlock);
    }

    private AutoCompleteTextView autoComplete;
    private PlacesArrayAdapter adapter;
    private OnDoneListener listener;

    public AddressBlock(Context context) {
        this(context, null, 0);
    }

    public AddressBlock(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AddressBlock(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void init(boolean autoMode, boolean showEmail) {
        if (!showEmail) findViewById(R.id.emailAddr).setVisibility(GONE);

        autoComplete = (AutoCompleteTextView) findViewById(R.id.addressACTV);
        adapter = new PlacesArrayAdapter(getContext(), R.layout.places_list_item);
        autoComplete.setAdapter(adapter);
        autoComplete.setOnItemClickListener(this);

        for(int id : addressFields) {
            ((EditText) findViewById(id)).setOnEditorActionListener(this);
        }
        selectMode(autoMode);
    }

    public void setOnDoneListener(OnDoneListener listener) {
        this.listener = listener;
    }

    private void selectMode(boolean autoMode) {
        int visibility;

        visibility = autoMode ? VISIBLE : GONE;
        findViewById(R.id.addressACTV).setVisibility(visibility);

        visibility = autoMode ? GONE : VISIBLE;
        for(int id : manualFields) {
            findViewById(id).setVisibility(visibility);
        }
    }

    // Validation

    private boolean validateRequired(int id) {
        TextView view = (TextView) findViewById(id);
        if (view==null || view.getVisibility()!=View.VISIBLE) return(true);

        String text = view.getText().toString().trim();
        if (text.length()==0) {
            view.setError(getContext().getResources().getString(R.string.required));
            return false;
        }
        view.setText(text);
        return true;
    }

    private boolean validateUsState(int id) {
        TextView view = (TextView) findViewById(id);
        if (view==null || view.getVisibility()!=View.VISIBLE) return(true);

        String text = view.getText().toString().trim();
        if (text.length()==0) {
            view.setError(getContext().getResources().getString(R.string.required));
            return(false);
        }
        if (UsState.findByAbbr(text)==null) {
            view.setError(getContext().getResources().getString(R.string.bad_us_state));
            return(false);
        }
        view.setText(text);
        return(true);
    }

    public boolean validate() {
        boolean valid = true;
        valid &= validateRequired(R.id.firstName);
        valid &= validateRequired(R.id.lastName);
        valid &= validateRequired(R.id.phoneNumber);
        valid &= validateRequired(R.id.emailAddr);
        valid &= validateRequired(R.id.addressACTV);
        valid &= validateRequired(R.id.addressET);
        valid &= validateRequired(R.id.city);
        valid &= validateUsState(R.id.state);
        valid &= validateRequired(R.id.zipCode);
        return (valid);
    }

    // Lower level methods

    private String extractField(int id) {
        TextView view = (TextView) findViewById(id);
        if (view==null) return(null);
        CharSequence text = view.getText();
        if (text.length()==0) return(null);
        return(text.toString());
    }

    private void injectField(int id, String text) {
        TextView view = (TextView) findViewById(id);
        if (view==null) return;
        if (text!=null) text = text.trim();
        view.setText(text);
    }

    // Methods for higher levels

    public ShippingAddress getShippingAddress() {
        ShippingAddress addr =new ShippingAddress();
        addr.setDeliveryFirstName(extractField(R.id.firstName));
        addr.setDeliveryLastName(extractField(R.id.lastName));
        addr.setDeliveryPhone(extractField(R.id.phoneNumber));
        addr.setEmailAddress(extractField(R.id.emailAddr));
        addr.setReenterEmailAddress(extractField(R.id.emailAddr));
        addr.setDeliveryAddress1(extractField(R.id.addressET));
        addr.setDeliveryCity(extractField(R.id.city));
        addr.setDeliveryState(extractField(R.id.state));
        addr.setDeliveryZipCode(extractField(R.id.zipCode));
        return(addr);
    }

    public String getEmailAddress() {
        return(extractField(R.id.emailAddr));
    }

    public void setAddress(Address addr) {
        injectField(R.id.firstName, addr.getFirstName());
        injectField(R.id.lastName, addr.getLastName());
        injectField(R.id.phoneNumber, addr.getPhone1());
        injectField(R.id.addressET, addr.getAddress1());
        injectField(R.id.city, addr.getCity());
        injectField(R.id.state, addr.getState());
        injectField(R.id.zipCode, addr.getZipCode());
    }

    public UpdateAddress getUpdateAddress() {
        // TODO no no-arg constructor
        UpdateAddress addr = new UpdateAddress();
        addr.setFirstName(extractField(R.id.firstName));
        addr.setLastName(extractField(R.id.lastName));
        addr.setPhoneNumber(extractField(R.id.phoneNumber));
        addr.setAddressLine1(extractField(R.id.addressET));
        addr.setCity(extractField(R.id.city));
        addr.setState(extractField(R.id.state));
        addr.setZipCode(extractField(R.id.zipCode));
        return(addr);
    }

    // Internal callbacks

    @Override
    public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
        switch(actionId) {
            case EditorInfo.IME_ACTION_DONE:
                if (!validate()) return(true);
                if (listener!=null)
                    listener.onDone(this);
                break;
            case EditorInfo.IME_NULL:
                if (event.getKeyCode()==KeyEvent.KEYCODE_ENTER &&
                        event.getAction()==KeyEvent.ACTION_DOWN) {
                    if (!validate()) return(true);
                    if (listener!=null)
                        listener.onDone(this);
                }
                break;
        }
        return(false);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//        activity.hideSoftKeyboard(view);
        String item = adapter.getItem(position);
        String inputManually = getResources().getString(R.string.input_manually_allcaps);
        if (item.equals(inputManually)) {
            selectMode(false);
        } else {
            adapter.getPlaceDetails(position, new PlacesArrayAdapter.PlaceDataCallback() {
                @Override
                public void onPlaceDataResult(PlacesArrayAdapter.PlaceData placeData) {
                    ((TextView) findViewById(R.id.addressET)).setText(placeData.streetAddress);
                    ((TextView) findViewById(R.id.city)).setText(placeData.city);
                    ((TextView) findViewById(R.id.state)).setText(placeData.state);
                    ((TextView) findViewById(R.id.zipCode)).setText(placeData.getFullZipCode());
                    String fullZipCode = placeData.getFullZipCode();
                    if (!TextUtils.isEmpty(fullZipCode)) {
                        autoComplete.setText(autoComplete.getText().toString() + " " + fullZipCode);
                    }
                    autoComplete.dismissDropDown();
                }
            });
        }

    }
}