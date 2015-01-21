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
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.staples.mobile.cfa.R;
import com.staples.mobile.cfa.profile.CreditCard;
import com.staples.mobile.cfa.profile.UsState;
import com.staples.mobile.common.access.easyopen.model.cart.BillingAddress;
import com.staples.mobile.common.access.easyopen.model.cart.PaymentMethod;
import com.staples.mobile.common.access.easyopen.model.cart.ShippingAddress;


public class GuestCheckoutFragment extends CheckoutFragment implements CompoundButton.OnCheckedChangeListener {
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
                            if (ccType != CreditCard.Type.UNKNOWN) {
                                cardImage.setImageResource(ccType.getImageResource());
                            }
                            expirationDateVw.setVisibility(View.VISIBLE);
                            cidVw.setVisibility(View.VISIBLE);
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
        EditText addressVw = (EditText)layoutView.findViewById(R.id.address);
        EditText cityVw = (EditText)layoutView.findViewById(R.id.city);
        EditText stateVw = (EditText)layoutView.findViewById(R.id.state);
        EditText phoneNumberVw = (EditText)layoutView.findViewById(R.id.phoneNumber);
        EditText zipCodeVw = (EditText)layoutView.findViewById(R.id.zipCode);

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
        if (ccType!=CreditCard.Type.STAPLES)
            if (!validateRequiredField(cidVw, requiredMsg)) { errors = true; }

        if (!errors) {
            PaymentMethod paymentMethod = new PaymentMethod();
            paymentMethod.setSaveCardIndicator("Y");
            paymentMethod.setCardNumber(cardNumberVw.getText().toString());
            paymentMethod.setCardType(ccType.getName());
            paymentMethod.setCardExpirationMonth(expirationDateVw.getText().toString().substring(0,2));
          paymentMethod.setCardExpirationYear("20" +expirationDateVw.getText().toString().substring(3,5));
            paymentMethod.setCardVerificationCode(cidVw.getText().toString());
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
                                Toast.makeText(activity, "Shipping address alert: " + infoMsg, Toast.LENGTH_LONG).show();
                            }

                            // now apply billing address
                            applyBillingAddressIfNeededAndPrecheckout();

                        } else {
                            // if shipping and tax already showing, need to hide them
                            resetShippingAndTax();

                            Toast.makeText(activity, errMsg, Toast.LENGTH_LONG).show();
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
                                Toast.makeText(activity, "Billing address alert: " + infoMsg, Toast.LENGTH_LONG).show();
                            }

                            // do precheckout
                            startPrecheckoutIfReady();

                        } else {
                            // if shipping and tax already showing, need to hide them
                            resetShippingAndTax();

                            Toast.makeText(activity, errMsg, Toast.LENGTH_LONG).show();
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
            Toast.makeText(activity, R.string.payment_method_required, Toast.LENGTH_LONG).show();
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
                    Toast.makeText(activity, errMsg, Toast.LENGTH_LONG).show();
                    Log.d(TAG, errMsg);
                }
            }
        });

    }
}
