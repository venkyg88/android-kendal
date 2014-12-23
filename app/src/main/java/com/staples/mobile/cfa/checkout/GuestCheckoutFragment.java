/*
 * Copyright (c) 2014 Staples, Inc. All rights reserved.
 */

package com.staples.mobile.cfa.checkout;

import android.content.res.Resources;
import android.os.Bundle;
import android.text.TextUtils;
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
import com.staples.mobile.cfa.profile.CardType;
import com.staples.mobile.cfa.profile.ProfileDetails;
import com.staples.mobile.common.access.Access;
import com.staples.mobile.common.access.config.StaplesAppContext;
import com.staples.mobile.common.access.easyopen.api.EasyOpenApi;
import com.staples.mobile.common.access.easyopen.model.ApiError;
import com.staples.mobile.common.access.easyopen.model.cart.BillingAddress;
import com.staples.mobile.common.access.easyopen.model.cart.PaymentMethod;
import com.staples.mobile.common.access.easyopen.model.cart.PaymentMethodResponse;
import com.staples.mobile.common.access.easyopen.model.cart.ShippingAddress;
import com.staples.mobile.common.access.easyopen.model.checkout.AddressValidationAlert;
import com.staples.mobile.common.access.easyopen.model.member.AddCreditCardPOW;
import com.staples.mobile.common.access.easyopen.model.member.POWResponse;

import java.util.ArrayList;
import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class GuestCheckoutFragment extends CheckoutFragment implements CompoundButton.OnCheckedChangeListener {
    private static final String TAG = GuestCheckoutFragment.class.getSimpleName();

    private static final int MAXFETCH = 50;

    private View guestEntryView;
    Switch useShipAddrAsBillingAddrSwitch;
    ViewGroup billingAddrContainer;
    View shippingAddrLayoutVw;
    View billingAddrLayoutVw;
    View paymentMethodLayoutVw;
    ImageView cardImage;
    EditText cardNumberVw;
    EditText expirationMonthVw;
    EditText expirationYearVw;
    EditText cidVw;
    EditText emailAddrVw;

    private boolean shippingAddrNeedsApplying = true;
    private boolean billingAddrNeedsApplying = true;

    /**
     * Create a new instance of GuestCheckoutFragment that will be initialized
     * with the given arguments.
     */
    public static CheckoutFragment newInstance(float couponsRewardsAmount, float itemSubtotal, float preTaxSubtotal) {
        CheckoutFragment f = new GuestCheckoutFragment();
        Bundle args = new Bundle();
        args.putFloat(CheckoutFragment.BUNDLE_PARAM_COUPONSREWARDS, couponsRewardsAmount);
        args.putFloat(CheckoutFragment.BUNDLE_PARAM_ITEMSUBTOTAL, itemSubtotal);
        args.putFloat(CheckoutFragment.BUNDLE_PARAM_PRETAXSUBTOTAL, preTaxSubtotal);
        f.setArguments(args);
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
        cardImage = (ImageView) view.findViewById(R.id.card_image);
        expirationMonthVw = (EditText)paymentMethodLayoutVw.findViewById(R.id.expirationMonth);
        expirationYearVw = (EditText)paymentMethodLayoutVw.findViewById(R.id.expirationYear);
        cidVw = (EditText)guestEntryView.findViewById(R.id.cid);
        emailAddrVw = (EditText)guestEntryView.findViewById(R.id.emailAddr);


        // hide imported views' Save buttons
        shippingAddrLayoutVw.findViewById(R.id.addressSaveBtn).setVisibility(View.GONE);
        billingAddrLayoutVw.findViewById(R.id.addressSaveBtn).setVisibility(View.GONE);
        paymentMethodLayoutVw.findViewById(R.id.addCCBtn).setVisibility(View.GONE);


        // TODO: replace with real trigger - TEMPORARY: when user completes CID, trigger off that to submit addresses if billing addr set to same as shipping addr
        cidVw.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (useShipAddrAsBillingAddrSwitch.isChecked()) {
                    applyAddresses();
                }
                return false;
            }
        });
        // TODO: replace with real trigger - TEMPORARY: when user completes billing addr zip code, trigger off that to submit addresses
        EditText zipCodeVw = (EditText)billingAddrLayoutVw.findViewById(R.id.zipCode);
        zipCodeVw.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                applyAddresses();
                return false;
            }
        });

        cardNumberVw.setOnEditorActionListener(
                new EditText.OnEditorActionListener() {

                    @Override
                    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                        if (actionId == EditorInfo.IME_ACTION_NEXT) {
                            CardType ccType = CardType.detect(cardNumberVw.getText().toString());
                            if (ccType != CardType.UNKNOWN) {
                                cardImage.setImageResource(ccType.getImageResource());
                            }
                            expirationMonthVw.requestFocus();
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
//        Toast.makeText(activity, "Checked: " + isChecked, Toast.LENGTH_SHORT).show();
        billingAddrContainer.setVisibility(isChecked? View.GONE: View.VISIBLE);
    }

    private boolean validateRequiredField(TextView textView, String msg) {
        if (TextUtils.isEmpty(textView.getText())) {
            textView.setError(msg);
            return false;
        }
        return true;
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

        // todo: remove this
        if (firstNameVw.getText().toString().equals("fake address")) {
            return getFakeShippingAddress();
        }

        // validate required fields
        String requiredMsg = resources.getString(R.string.required);
        if (!validateRequiredField(firstNameVw, requiredMsg)) { errors = true; }
        if (!validateRequiredField(firstNameVw, requiredMsg)) { errors = true; }
        if (!validateRequiredField(lastNameVw, requiredMsg)) { errors = true; }
        if (!validateRequiredField(addressVw, requiredMsg)) { errors = true; }
        if (!validateRequiredField(cityVw, requiredMsg)) { errors = true; }
        if (!validateRequiredField(stateVw, requiredMsg)) { errors = true; }
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
            shippingAddress.setEmailAddress(emailAddrVw.getText().toString());
            shippingAddress.setReenterEmailAddress(emailAddrVw.getText().toString()); // Kavitha says don't make the user re-enter address, just copy same addr here
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
        if (!validateRequiredField(expirationMonthVw, requiredMsg)) { errors = true; }
        if (!validateRequiredField(expirationYearVw, requiredMsg)) { errors = true; }
        CardType ccType = CardType.detect(cardNumberVw.getText().toString());
        if (ccType!=CardType.STAPLES)
            if (!validateRequiredField(cidVw, requiredMsg)) { errors = true; }

        if (!errors) {
            PaymentMethod paymentMethod = new PaymentMethod();
            paymentMethod.setSaveCardIndicator("Y");
            paymentMethod.setCardNumber(cardNumberVw.getText().toString());
            paymentMethod.setCardType(ccType.getCardTypeName());
            paymentMethod.setCardExpirationMonth(expirationMonthVw.getText().toString());
            paymentMethod.setCardExpirationYear(expirationYearVw.getText().toString());
            paymentMethod.setCardVerificationCode(cidVw.getText().toString());
            return paymentMethod;
        }
        return null;
    }

    /** TODO: remove this - gets fake shipping address */
    private ShippingAddress getFakeShippingAddress() {
        ShippingAddress shippingAddress = new ShippingAddress();
        shippingAddress.setDeliveryFirstName("Diana");
        shippingAddress.setDeliveryLastName("Sutlief");
        shippingAddress.setDeliveryAddress1("3614 Delverne RD");
        shippingAddress.setDeliveryCity("Baltimore");
        shippingAddress.setDeliveryState("MD");
        shippingAddress.setDeliveryZipCode("21218");
        shippingAddress.setDeliveryPhone("206-362-8024");
        shippingAddress.setEmailAddress("diana.sutlief@staples.com");
        shippingAddress.setReenterEmailAddress("diana.sutlief@staples.com");
        return shippingAddress;
    }


    private void applyAddresses() {
        shippingAddrNeedsApplying = true;
        billingAddrNeedsApplying = true;
        applyShippingAddress();
        applyBillingAddress();
    }

    private void applyShippingAddress() {
        ShippingAddress shippingAddress = getShippingAddress();

        // add shipping address to cart
        if (shippingAddress != null) {
            showProgressIndicator();
            secureApi.addShippingAddressToCart(shippingAddress,
                    new Callback<AddressValidationAlert>() {

                        @Override
                        public void success(AddressValidationAlert precheckoutResponse, Response response) {
                            String validationAlert = precheckoutResponse.getAddressValidationAlert();

                            if (validationAlert != null) {
                                Toast.makeText(activity, "Shipping address alert: " + validationAlert, Toast.LENGTH_SHORT).show();
                            } else {
                                shippingAddrNeedsApplying = false;
                                new ProfileDetails().refreshProfile(null); // refresh cached profile
                            }
                            startPrecheckoutIfReady();
                        }

                        @Override
                        public void failure(RetrofitError retrofitError) {
                            String msg = "Shipping address error: " + ApiError.getErrorMessage(retrofitError);
                            Log.d(TAG, msg);
                            Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show();
                            hideProgressIndicator();
                        }
                    });
        }
    }

    private void applyBillingAddress() {
        BillingAddress billingAddress = getBillingAddress();

        // add shipping address to cart
        if (billingAddress != null) {
            showProgressIndicator();
            secureApi.addBillingAddressToCart(billingAddress,
                    new Callback<AddressValidationAlert>() {

                        @Override
                        public void success(AddressValidationAlert precheckoutResponse, Response response) {
                            String validationAlert = precheckoutResponse.getAddressValidationAlert();

                            if (validationAlert != null) {
                                Toast.makeText(activity, "Billing address alert: " + validationAlert, Toast.LENGTH_SHORT).show();
                            } else {
                                billingAddrNeedsApplying = false;
                            }
                            startPrecheckoutIfReady();
                        }

                        @Override
                        public void failure(RetrofitError retrofitError) {
                            String msg = "Billing address error: " + ApiError.getErrorMessage(retrofitError);
                            Log.d(TAG, msg);
                            Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show();
                            hideProgressIndicator();
                        }
                    });
        } else {
            hideProgressIndicator();
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
        submitPaymentMethod();
    }

    private void submitPaymentMethod() {

        PaymentMethod paymentMethod = getPaymentMethod();
        if (paymentMethod==null) {
            Toast.makeText(activity, R.string.payment_method_required, Toast.LENGTH_SHORT).show();
            return;
        }

        // first add selected payment method to cart
        showProgressIndicator();
        // encrypt payment method
        String powCardType = paymentMethod.getCardType();
        AddCreditCardPOW creditCard = new AddCreditCardPOW(paymentMethod.getCardNumber(), powCardType);
        List<AddCreditCardPOW> ccList = new ArrayList<AddCreditCardPOW>();
        ccList.add(creditCard);

        // TODO find a better way to determine current environment
        if (StaplesAppContext.getInstance().getEasyOpenApiUrl().equals("api.staples.com"))
        {
            EasyOpenApi powApi = Access.getInstance().getPOWApi(true);
            powApi.addCreditPOWCall(ccList, new PowListener(paymentMethod));
        } else {
            secureApi.addCreditPOWCallQA(ccList, new PowListener(paymentMethod));
        }
    }

    class PowListener implements Callback<List<POWResponse>> {

        PaymentMethod paymentMethod;

        PowListener(PaymentMethod paymentMethod) {
            this.paymentMethod = paymentMethod;
        }

        @Override
        public void success(List<POWResponse> powList, Response response) {
            Log.d(TAG, powList.size()+" Packet: "+powList.get(0).getPacket());
            if ("0".equals(powList.get(0).getStatus()) && !TextUtils.isEmpty(powList.get(0).getPacket())) {
                paymentMethod.setCardNumber(powList.get(0).getPacket());

                // add payment method to cart
                secureApi.addPaymentMethodToCart(paymentMethod,
                        new retrofit.Callback<PaymentMethodResponse>() {
                            @Override
                            public void success(PaymentMethodResponse paymentMethodResponse, Response response) {
                                // upon payment method success, submit the order
                                submitOrder(paymentMethod.getCardVerificationCode());
                                new ProfileDetails().refreshProfile(null); // refresh cached profile
                            }

                            @Override
                            public void failure(RetrofitError retrofitError) {
                                String msg = "Payment Error #1: " + ApiError.getErrorMessage(retrofitError);
                                Log.d(TAG, msg);
                                Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show();
                                hideProgressIndicator();
                            }
                        }
                );
            } else {
                String msg = "Payment Error #2: Status="+powList.get(0).getStatus();
                Log.d(TAG, msg);
                Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show();
                hideProgressIndicator();
            }
        }

        @Override
        public void failure(RetrofitError retrofitError) {
            hideProgressIndicator();
            Toast.makeText(activity, "Payment Error #3: " + ApiError.getErrorMessage(retrofitError), Toast.LENGTH_SHORT).show();
        }
    }

}
