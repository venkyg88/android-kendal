package com.staples.mobile.cfa.widget;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.staples.mobile.cfa.R;
import com.staples.mobile.cfa.profile.UsState;
import com.staples.mobile.common.access.easyopen.model.cart.ShippingAddress;
import com.staples.mobile.common.access.easyopen.model.member.Address;
import com.staples.mobile.common.access.easyopen.model.member.UpdateAddress;

public class AddressBlock extends LinearLayout implements TextView.OnEditorActionListener, PlaceFieldView.OnPlaceDoneListener {
    private static final String TAG = AddressBlock.class.getSimpleName();

    private static final int[] addressFields = {R.id.firstName, R.id.lastName, R.id.phoneNumber, R.id.emailAddr,
                                                R.id.apartment, R.id.city, R.id.state, R.id.zipCode};
    private static final int[] manualFields = {R.id.apartment, R.id.city, R.id.state, R.id.zipCode};

    public interface OnDoneListener {
        void onDone(AddressBlock addressBlock, boolean valid);
        void onNext(AddressBlock addressBlock);
    }

    private PlaceFieldView placeFieldView;
    private OnDoneListener listener;
    EditText phoneNumberView;
    PhoneNumberFormattingTextWatcher phoneNumberFormattingTextWatcher;

    public AddressBlock(Context context) {
        this(context, null, 0);
    }

    public AddressBlock(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AddressBlock(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void init(boolean showEmail) {
        if (!showEmail) findViewById(R.id.emailAddr).setVisibility(GONE);

        placeFieldView = (PlaceFieldView) findViewById(R.id.address);
        placeFieldView.setOnPlaceDoneListener(this);

        for(int id : addressFields) {
            ((EditText) findViewById(id)).setOnEditorActionListener(this);
        }

        // set up phone input mask
        phoneNumberView = (EditText) findViewById(R.id.phoneNumber);
        phoneNumberFormattingTextWatcher = new PhoneNumberFormattingTextWatcher();
        phoneNumberView.addTextChangedListener(phoneNumberFormattingTextWatcher);
        // profile calls init() after setting value, so need to call this here
        phoneNumberFormattingTextWatcher.afterTextChanged(phoneNumberView.getEditableText()); // fake a text change to initialize format

        selectMode(true);
    }

    public void setOnDoneListener(OnDoneListener listener) {
        this.listener = listener;
    }

    public void selectMode(boolean autoMode) {
        placeFieldView.selectMode(autoMode);
        int visibility = autoMode ? GONE : VISIBLE;
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
        valid &= validateRequired(R.id.address);
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
        addr.setDeliveryPhone(extractField(R.id.phoneNumber).replaceAll("[^0-9]", "")); // remove characters added by input mask
        addr.setEmailAddress(extractField(R.id.emailAddr));
        addr.setReenterEmailAddress(extractField(R.id.emailAddr));
        addr.setDeliveryAddress1(extractField(R.id.address));
        addr.setDeliveryAddress2(extractField(R.id.apartment));
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
        if (phoneNumberFormattingTextWatcher != null) { // profile calls init() later
            phoneNumberFormattingTextWatcher.afterTextChanged(phoneNumberView.getEditableText()); // fake a text change to initialize format
        }
        injectField(R.id.address, addr.getAddress1());
        injectField(R.id.apartment, addr.getAddress2());
        injectField(R.id.city, addr.getCity());
        injectField(R.id.state, addr.getState());
        injectField(R.id.zipCode, addr.getZipCode());
    }

    public UpdateAddress getUpdateAddress() {
        UpdateAddress addr = new UpdateAddress();
        addr.setFirstName(extractField(R.id.firstName));
        addr.setLastName(extractField(R.id.lastName));
        addr.setPhoneNumber(extractField(R.id.phoneNumber).replaceAll("[^0-9]", "")); // remove characters added by input mask
        addr.setAddressLine1(extractField(R.id.address));
        addr.setAddressLine2(extractField(R.id.apartment));
        addr.setCity(extractField(R.id.city));
        addr.setState(extractField(R.id.state));
        addr.setZipCode(extractField(R.id.zipCode));
        return(addr);
    }

    // Internal callbacks

    @Override
    public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
        boolean valid;
        switch(actionId) {
            case EditorInfo.IME_ACTION_NEXT:
                if (listener!=null) {
                    listener.onNext(this);
                }
                break;
            case EditorInfo.IME_ACTION_DONE:
                valid = validate();
                if (listener!=null) {
                    listener.onDone(this, valid);
                }
                break;
            case EditorInfo.IME_NULL:
                if (event.getKeyCode()==KeyEvent.KEYCODE_ENTER &&
                        event.getAction()==KeyEvent.ACTION_DOWN) {
                    valid = validate();
                    if (listener!=null) {
                        listener.onDone(this, valid);
                    }
                }
                break;
        }
        // if text, clear previous error
        if (!TextUtils.isEmpty(view.getText())) {
            view.setError(null);
        }
        return(false);
    }

    @Override
    public void onPlaceDone(PlaceFieldView.Place place) {
        selectMode(false);
        if (place!=null) {
            injectField(R.id.address, place.streetAddress);
            injectField(R.id.city, place.city);
            injectField(R.id.state, place.state);
            injectField(R.id.zipCode, place.postalCode);
        }
    }
}
