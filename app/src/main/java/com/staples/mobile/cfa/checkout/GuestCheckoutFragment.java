/*
 * Copyright (c) 2014 Staples, Inc. All rights reserved.
 */

package com.staples.mobile.cfa.checkout;

import android.content.res.Resources;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.staples.mobile.cfa.R;
import com.staples.mobile.cfa.profile.CreditCard;
import com.staples.mobile.cfa.profile.PlacesArrayAdapter;
import com.staples.mobile.cfa.profile.UsState;
import com.staples.mobile.common.access.easyopen.model.cart.BillingAddress;
import com.staples.mobile.common.access.easyopen.model.cart.PaymentMethod;
import com.staples.mobile.common.access.easyopen.model.cart.ShippingAddress;


public class GuestCheckoutFragment extends CheckoutFragment implements CompoundButton.OnCheckedChangeListener,
        View.OnFocusChangeListener {

    private static final String TAG = GuestCheckoutFragment.class.getSimpleName();

    private View guestEntryView;
    Switch useShipAddrAsBillingAddrSwitch;
    ViewGroup billingAddrContainer;
    View shippingAddrLayoutVw;
    View billingAddrLayoutVw;
    View paymentMethodLayoutVw;
    ImageView cardImage;
    EditText cardNumberVw;
    EditText expirationDateVw;
    EditText cidVw;
    EditText emailAddrVw;
    EditText shippingZipCodeVw;
    EditText billingZipCodeVw;

    private boolean shippingAddrNeedsApplying = true;
    private boolean billingAddrNeedsApplying = true;

    private String emailAddress;

    // autocomplete data
    PlacesArrayAdapter.PlaceData shippingPlaceData;
    PlacesArrayAdapter.PlaceData billingPlaceData;

    /**
     * Create a new instance of GuestCheckoutFragment that will be initialized
     * with the given arguments.
     */
    public static CheckoutFragment newInstance(float couponsRewardsAmount, float itemSubtotal, float preTaxSubtotal, String deliveryRange) {
        CheckoutFragment f = new GuestCheckoutFragment();
        f.setArguments(createInitialBundle(couponsRewardsAmount, itemSubtotal, preTaxSubtotal, deliveryRange));
        return f;
    }

    /** specifies layout for variable entry area */
    @Override
    protected int getEntryLayoutId() {
        return R.layout.checkout_guest_entry;
    }

    /** initializes variable entry area of checkout screen */
    @Override
    protected void initEntryArea(View view) {

        guestEntryView = view;
        shippingAddrLayoutVw = view.findViewById(R.id.shipping_addr_layout);
        billingAddrLayoutVw = view.findViewById(R.id.billing_addr_layout);
        billingAddrContainer = (ViewGroup)view.findViewById(R.id.billing_addr_container);
        paymentMethodLayoutVw = view.findViewById(R.id.payment_method_layout);
        cardNumberVw = (EditText)paymentMethodLayoutVw.findViewById(R.id.cardNumber);
        cardImage = (ImageView) paymentMethodLayoutVw.findViewById(R.id.card_image);
        expirationDateVw = (EditText)paymentMethodLayoutVw.findViewById(R.id.expirationDate);
        cidVw = (EditText)paymentMethodLayoutVw.findViewById(R.id.cid);
        emailAddrVw = (EditText)guestEntryView.findViewById(R.id.emailAddr);


        // hide imported views' Save buttons
        shippingAddrLayoutVw.findViewById(R.id.addressSaveBtn).setVisibility(View.GONE);
        billingAddrLayoutVw.findViewById(R.id.addressSaveBtn).setVisibility(View.GONE);
        paymentMethodLayoutVw.findViewById(R.id.addCCBtn).setVisibility(View.GONE);
        paymentMethodLayoutVw.findViewById(R.id.cancelCCBtn).setVisibility(View.GONE);


        // set up address widgets including auto-complete
        setupAddressWidgets(true); // shipping
        setupAddressWidgets(false); // billing


        // on any change to addresses, apply addresses to cart and do precheckout
        // TODO: replace triggers on zip code with a real trigger on any change to addresses
        shippingZipCodeVw = (EditText)shippingAddrLayoutVw.findViewById(R.id.zipCode);
        billingZipCodeVw = (EditText)billingAddrLayoutVw.findViewById(R.id.zipCode);
        shippingZipCodeVw.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                shippingAddrNeedsApplying = true;
                if (useShipAddrAsBillingAddrSwitch.isChecked()) {
                    billingAddrNeedsApplying = true;
                }
                applyAddressesAndPrecheckout();
                return false;
            }
        });
        billingZipCodeVw.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                billingAddrNeedsApplying = true;
                applyAddressesAndPrecheckout();
                return false;
            }
        });

        expirationDateVw.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(start == 1 && before < start) {
                    expirationDateVw.setText(s+"/");
                    expirationDateVw.setSelection(expirationDateVw.getText().length());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }});

        cardNumberVw.setOnEditorActionListener(
                new EditText.OnEditorActionListener() {

                    @Override
                    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                        if (actionId == EditorInfo.IME_ACTION_NEXT) {
                            CreditCard.Type ccType = CreditCard.Type.detect(cardNumberVw.getText().toString());
                            cardImage.setImageResource(ccType.getImageResource());
                            expirationDateVw.setVisibility(View.VISIBLE);
                            if (ccType.isCidUsed()) {
                                cidVw.setVisibility(View.VISIBLE);
                                expirationDateVw.setImeOptions(EditorInfo.IME_ACTION_NEXT);
                            }
                            expirationDateVw.requestFocus();
                            return true; // consume.
                        }
                        return false; // pass on to other listeners.
                    }
                });

        cardNumberVw.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (MotionEvent.ACTION_UP == event.getAction()) {
                    cardNumberVw.getText().clear();
                    cardImage.setImageResource(0);
                }
                return false; // return is important...
            }
        });

        // add listener to billing addr toggle button switch
        useShipAddrAsBillingAddrSwitch = (Switch)view.findViewById(R.id.useShipAddrAsBillingAddr_switch);
        useShipAddrAsBillingAddrSwitch.setChecked(true);
        useShipAddrAsBillingAddrSwitch.setOnCheckedChangeListener(this);

    }

    private void setupAddressWidgets(final boolean shipping) {
        final View layoutView = shipping? shippingAddrLayoutVw : billingAddrLayoutVw;
        final AutoCompleteTextView addressAutocompleteVw = (AutoCompleteTextView) layoutView.findViewById(R.id.addressACTV);
        final PlacesArrayAdapter placesArrayAdapter = new PlacesArrayAdapter(activity, R.layout.places_list_item);
        addressAutocompleteVw.setAdapter(placesArrayAdapter);
        addressAutocompleteVw.setFocusable(true);
        addressAutocompleteVw.setFocusableInTouchMode(true);

        setupLabeledEditText(layoutView, R.id.firstName, R.id.firstNameLabel);
        setupLabeledEditText(layoutView, R.id.lastName, R.id.lastNameLabel);
        setupLabeledEditText(layoutView, R.id.phoneNumber, R.id.phoneLabel);
        setupLabeledEditText(layoutView, R.id.addressACTV, R.id.addressLabel);
        setupLabeledEditText(layoutView, R.id.addressET, R.id.addressLabel);
        setupLabeledEditText(layoutView, R.id.apartment, R.id.apartmentLabel);
        setupLabeledEditText(layoutView, R.id.city, R.id.cityLabel);
        setupLabeledEditText(layoutView, R.id.state, R.id.stateLabel);
        setupLabeledEditText(layoutView, R.id.zipCode, R.id.zipCodeLabel);
        if (shipping) {
            // make email address visible
            setupLabeledEditText(layoutView, R.id.emailAddr, R.id.emailAddrLabel);
            layoutView.findViewById(R.id.emailAddr).setVisibility(View.VISIBLE);
            layoutView.findViewById(R.id.emailAddrLabel).setVisibility(View.INVISIBLE);

            // move address label to below email address
            View addressLabel = layoutView.findViewById(R.id.addressLabel);
            RelativeLayout.LayoutParams addressLabelParams = (RelativeLayout.LayoutParams)addressLabel.getLayoutParams();
            addressLabelParams.addRule(RelativeLayout.BELOW, R.id.emailAddr);
            addressLabel.setLayoutParams(addressLabelParams); //causes layout update
        }

        // handle item clicks
        addressAutocompleteVw.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                activity.hideSoftKeyboard(view);
                String inputManually = getResources().getString(R.string.input_manually_allcaps);
                String resultItem = placesArrayAdapter.getItem(position);
                if (resultItem.equals(inputManually) ) {
                    addressAutocompleteVw.setVisibility(View.GONE);
                    showAddressManualInputs(layoutView);
                } else {
                    placesArrayAdapter.getPlaceDetails(position, new PlacesArrayAdapter.PlaceDataCallback() {
                        @Override
                        public void onPlaceDataResult(PlacesArrayAdapter.PlaceData placeData) {
                            if (shipping) {
                                shippingPlaceData = placeData;
                            } else {
                                billingPlaceData = placeData;
                            }
                            String fullZipCode = placeData.getFullZipCode();
                            if (!TextUtils.isEmpty(fullZipCode)) {
                                addressAutocompleteVw.setText(addressAutocompleteVw.getText().toString() + " " + fullZipCode);
                            }
                            addressAutocompleteVw.dismissDropDown();
                        }
                    });
                }
            }
        });
    }

//    If billing address matches shipping, then when the last credit card field is filled out (CID if present, otherwise expiration)
//    When the billing autocomplete address is selected
//    When the billing manual entry zip code field is filled out
//    When any field has been modified in any way after a previous precheckout has been completed (this could get extremely annoying but not sure what alternative there is)

    private void setupLabeledEditText(View layoutView, int resourceId, int labelResourceId) {
        View editText = layoutView.findViewById(resourceId);
        View labelVw = layoutView.findViewById(labelResourceId);
        editText.setTag(labelVw);
        editText.setOnFocusChangeListener(this);
    }

    private void showAddressManualInputs(View layoutView) {

        // change labels from GONE to INVISIBLE so that they take up space
        // change inputs to VISIBLE

        View manualAddressVw = layoutView.findViewById(R.id.addressET);
        manualAddressVw.setVisibility(View.VISIBLE);
        manualAddressVw.requestFocus();

        layoutView.findViewById(R.id.apartmentLabel).setVisibility(View.INVISIBLE);
        layoutView.findViewById(R.id.apartment).setVisibility(View.VISIBLE);

        layoutView.findViewById(R.id.cityLabel).setVisibility(View.INVISIBLE);
        layoutView.findViewById(R.id.city).setVisibility(View.VISIBLE);

        layoutView.findViewById(R.id.stateLabel).setVisibility(View.INVISIBLE);
        layoutView.findViewById(R.id.state).setVisibility(View.VISIBLE);

        layoutView.findViewById(R.id.zipCodeLabel).setVisibility(View.INVISIBLE);
        layoutView.findViewById(R.id.zipCode).setVisibility(View.VISIBLE);

        layoutView.findViewById(R.id.apartmentLabel).setVisibility(View.INVISIBLE);
        layoutView.findViewById(R.id.apartment).setVisibility(View.VISIBLE);

        layoutView.findViewById(R.id.apartmentLabel).setVisibility(View.INVISIBLE);
        layoutView.findViewById(R.id.apartment).setVisibility(View.VISIBLE);
    }

    @Override
    public void onFocusChange(View view, boolean hasFocus) {

        if (hasFocus) {
            Object viewTag = view.getTag();
            if (viewTag != null) {
                if (view instanceof EditText) {
                    ((EditText) view).setHint("");
                }
                if (viewTag instanceof TextView) {
                    ((TextView) viewTag).setVisibility(View.VISIBLE);
                }
            }
        }
    }

    /** implements CompoundButton.OnCheckedChangeListener */
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        billingAddrContainer.setVisibility(isChecked? View.GONE: View.VISIBLE);
        billingAddrNeedsApplying = true;
        // if checked and shipping addr successfully applied, then apply billing and proceed to precheckout
        if (isChecked && !shippingAddrNeedsApplying) {
            applyAddressesAndPrecheckout();
        // else if unchecked and previously filled in address, then apply addresses and do precheckout
        } else if (!isChecked && !TextUtils.isEmpty(billingZipCodeVw.getText())) {
            applyAddressesAndPrecheckout();
        } else {
            // otherwise just reset shipping/tax info and wait for user to fill out necessary info
            resetShippingAndTax();
        }
    }

    private boolean validateRequiredField(TextView textView, String msg) {
        if (TextUtils.isEmpty(textView.getText())) {
            textView.setError(msg);
            return false;
        }
        return true;
    }

    private boolean validateUsState(TextView textView, String requiredMsg, String badUsStateMsg) {
        if (textView.getText().length()==0) {
            textView.setError(requiredMsg);
            return(false);
        }
        if (UsState.findByAbbr(textView.getText().toString())==null) {
            textView.setError(badUsStateMsg);
            return(false);
        }
        return(true);
    }

    /** gets shipping address from user's entries */
    private ShippingAddress getShippingAddress(View layoutView, boolean includeEmail) {
        Resources resources = getResources();
        boolean errors = false;

        EditText firstNameVw = (EditText)layoutView.findViewById(R.id.firstName);
        EditText lastNameVw = (EditText)layoutView.findViewById(R.id.lastName);
        EditText addressVw = (EditText)layoutView.findViewById(R.id.addressET);
        EditText apartmentVw = (EditText)layoutView.findViewById(R.id.apartment);
        EditText cityVw = (EditText)layoutView.findViewById(R.id.city);
        EditText stateVw = (EditText)layoutView.findViewById(R.id.state);
        EditText phoneNumberVw = (EditText)layoutView.findViewById(R.id.phoneNumber);
        EditText zipCodeVw = (EditText)layoutView.findViewById(R.id.zipCode);

        AutoCompleteTextView addressLineACTV = (AutoCompleteTextView) layoutView.findViewById(R.id.addressACTV);

        // validate required fields
        String requiredMsg = resources.getString(R.string.required);
        String badUsStateMsg = resources.getString(R.string.bad_us_state);

        if (!validateRequiredField(firstNameVw, requiredMsg)) { errors = true; }
        if (!validateRequiredField(firstNameVw, requiredMsg)) { errors = true; }
        if (!validateRequiredField(lastNameVw, requiredMsg)) { errors = true; }
        if (!validateRequiredField(addressVw, requiredMsg)) { errors = true; }
        if (!validateRequiredField(cityVw, requiredMsg)) { errors = true; }
        if (!validateUsState(stateVw, requiredMsg, badUsStateMsg)) { errors = true; }
        if (!validateRequiredField(phoneNumberVw, requiredMsg)) { errors = true; }
        if (!validateRequiredField(zipCodeVw, requiredMsg)) { errors = true; }

        ShippingAddress shippingAddress = new ShippingAddress();
        shippingAddress.setDeliveryFirstName(firstNameVw.getText().toString());
        shippingAddress.setDeliveryLastName(lastNameVw.getText().toString());
        shippingAddress.setDeliveryAddress1(addressVw.getText().toString());
        shippingAddress.setDeliveryAddress2(apartmentVw.getText().toString());
        shippingAddress.setDeliveryCity(cityVw.getText().toString());
        shippingAddress.setDeliveryState(stateVw.getText().toString());
        shippingAddress.setDeliveryPhone(phoneNumberVw.getText().toString());
        shippingAddress.setDeliveryZipCode(zipCodeVw.getText().toString());

        if (includeEmail) {
            emailAddress = emailAddrVw.getText().toString(); // set member variable since we'll need it later in order confirmation
            shippingAddress.setEmailAddress(emailAddress);
            shippingAddress.setReenterEmailAddress(emailAddress); // Kavitha says don't make the user re-enter address, just copy same addr here
            if (!validateRequiredField(emailAddrVw, requiredMsg)) { errors = true; }
        }
        return errors? null:shippingAddress;
    }

    /** gets shipping address from user's entries */
    private ShippingAddress getShippingAddress() {
        return getShippingAddress(shippingAddrLayoutVw, true);
    }

    /** gets billing address from user's entries */
    private BillingAddress getBillingAddress() {
        ShippingAddress shippingAddress = getShippingAddress(useShipAddrAsBillingAddrSwitch.isChecked()?
                shippingAddrLayoutVw : billingAddrLayoutVw, false);
        return (shippingAddress == null)? null : new BillingAddress(shippingAddress);
    }

    /** gets payment method from user's entries */
    private PaymentMethod getPaymentMethod() {
        Resources resources = getResources();
        boolean errors = false;

        // validate required fields
        String requiredMsg = resources.getString(R.string.required);
        if (!validateRequiredField(cardNumberVw, requiredMsg)) { errors = true; }
        if (!validateRequiredField(expirationDateVw, requiredMsg)) { errors = true; }
        CreditCard.Type ccType = CreditCard.Type.detect(cardNumberVw.getText().toString());
        if (ccType.isCidUsed()) {
            if (!validateRequiredField(cidVw, requiredMsg)) {
                errors = true;
            }
        }

        if (!errors) {
            PaymentMethod paymentMethod = new PaymentMethod();
            paymentMethod.setSaveCardIndicator("Y");
            paymentMethod.setCardNumber(cardNumberVw.getText().toString());
            paymentMethod.setCardType(ccType.getName());
            paymentMethod.setCardExpirationMonth(expirationDateVw.getText().toString().substring(0,2));
            paymentMethod.setCardExpirationYear("20" +expirationDateVw.getText().toString().substring(3,5));
            if (ccType.isCidUsed()) {
                paymentMethod.setCardVerificationCode(cidVw.getText().toString());
            }
            return paymentMethod;
        }
        return null;
    }

    private void applyAddressesAndPrecheckout() {

        // add shipping address to cart if necessary, then billing address and precheckout
        if (shippingAddrNeedsApplying) {
            ShippingAddress shippingAddress = getShippingAddress();
            if (shippingAddress != null) {
                showProgressIndicator();
                CheckoutApiManager.applyShippingAddress(shippingAddress, new CheckoutApiManager.ApplyAddressCallback() {
                    @Override
                    public void onApplyAddressComplete(String addressId, String errMsg, String infoMsg) {
                        hideProgressIndicator();

                        // if success
                        if (errMsg == null) {
                            shippingAddrNeedsApplying = false;

                            if (infoMsg != null) {
                                activity.showErrorDialog("Shipping address alert: " + infoMsg);
                            }

                            // now apply billing address
                            applyBillingAddressIfNeededAndPrecheckout();

                        } else {
                            // if shipping and tax already showing, need to hide them
                            resetShippingAndTax();

                            activity.showErrorDialog(errMsg);
                            Log.d(TAG, errMsg);
                        }
                    }
                });
            }
        } else {
            applyBillingAddressIfNeededAndPrecheckout();
        }
    }

    private void applyBillingAddressIfNeededAndPrecheckout() {

        // apply billing address to cart if necessary
        if (billingAddrNeedsApplying) {
            BillingAddress billingAddress = getBillingAddress();
            if (billingAddress != null) {
                showProgressIndicator();
                CheckoutApiManager.applyBillingAddress(billingAddress, new CheckoutApiManager.ApplyAddressCallback() {
                    @Override
                    public void onApplyAddressComplete(String addressId, String errMsg, String infoMsg) {
                        hideProgressIndicator();

                        // if success
                        if (errMsg == null) {
                            billingAddrNeedsApplying = false;

                            if (infoMsg != null) {
                                activity.showErrorDialog("Billing address alert: " + infoMsg);
                            }

                            // do precheckout
                            startPrecheckoutIfReady();

                        } else {
                            // if shipping and tax already showing, need to hide them
                            resetShippingAndTax();

                            activity.showErrorDialog(errMsg);
                            Log.d(TAG, errMsg);
                        }
                    }
                });
            }
        } else {
            // do precheckout
            startPrecheckoutIfReady();
        }
    }


    private void startPrecheckoutIfReady() {
        if (!shippingAddrNeedsApplying && !billingAddrNeedsApplying) {
            startPrecheckout();
        }
    }

    /** handles order submission */
    @Override
    protected void onSubmit() {
        submitPaymentMethodAndOrder();
    }

    private void submitPaymentMethodAndOrder() {

        final PaymentMethod paymentMethod = getPaymentMethod();
        if (paymentMethod==null) {
            activity.showErrorDialog(R.string.payment_method_required);
            return;
        }

        showProgressIndicator();
        // encrypt and apply payment method
        CheckoutApiManager.encryptAndApplyPaymentMethod(paymentMethod, new CheckoutApiManager.ApplyPaymentMethodCallback() {
            @Override
            public void onApplyPaymentMethodComplete(String paymentMethodId, String authorized, String errMsg) {
                hideProgressIndicator();

                // if success
                if (errMsg == null) {

                    // submit the order
                    submitOrder(paymentMethod.getCardVerificationCode(), emailAddress);

                } else {
                    activity.showErrorDialog(errMsg);
                    Log.d(TAG, errMsg);
                }
            }
        });

    }
}
