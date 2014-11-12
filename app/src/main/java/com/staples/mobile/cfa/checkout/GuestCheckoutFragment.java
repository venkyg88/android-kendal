/*
 * Copyright (c) 2014 Staples, Inc. All rights reserved.
 */

package com.staples.mobile.cfa.checkout;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import com.staples.mobile.R;
import com.staples.mobile.cfa.login.LoginHelper;
import com.staples.mobile.common.access.easyopen.model.ApiError;
import com.staples.mobile.common.access.easyopen.model.cart.BillingAddress;
import com.staples.mobile.common.access.easyopen.model.cart.PaymentMethod;
import com.staples.mobile.common.access.easyopen.model.cart.PaymentMethodResponse;
import com.staples.mobile.common.access.easyopen.model.cart.ShippingAddress;
import com.staples.mobile.common.access.easyopen.model.checkout.AddressValidationAlert;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class GuestCheckoutFragment extends CheckoutFragment implements CompoundButton.OnCheckedChangeListener {
    private static final String TAG = GuestCheckoutFragment.class.getSimpleName();

    private static final String RECOMMENDATION = "v1";
    private static final String STORE_ID = "10001";

    private static final String CATALOG_ID = "10051";
    private static final String LOCALE = "en_US";

    private static final String CLIENT_ID = LoginHelper.CLIENT_ID;

    private static final int MAXFETCH = 50;

    private View guestEntryView;
    Switch useShipAddrAsBillingAddrSwitch;
    ViewGroup billingAddrContainer;

    private boolean shippingAddrNeedsApplying = true;
    private boolean billingAddrNeedsApplying = true;


    /** override this to specify layout for entry area */
    @Override
    protected int getEntryLayoutId() {
        return R.layout.checkout_guest_entry;
    }

    /** override this for variation on entry area */
    @Override
    protected void initEntryArea(View view) {

        guestEntryView = view;

        // if logged in as guest, show sign-in button
        LoginHelper loginHelper = new LoginHelper(activity);
        if (loginHelper.isLoggedIn() && loginHelper.isGuestLogin()) {
            view.findViewById(R.id.signin_button).setVisibility(View.VISIBLE);
        }

        // hide imported view's Save button
        View shippingAddrLayoutVw = view.findViewById(R.id.shipping_addr_layout);
        shippingAddrLayoutVw.findViewById(R.id.addressSaveBtn).setVisibility(View.GONE);
        View billingAddrLayoutVw = view.findViewById(R.id.billing_addr_layout);
        billingAddrLayoutVw.findViewById(R.id.addressSaveBtn).setVisibility(View.GONE);

        billingAddrContainer = (ViewGroup)view.findViewById(R.id.billing_addr_container);


        // use temp button for now to fake address entry
        View temporaryButton = view.findViewById(R.id.temp_button);
        temporaryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shippingAddrNeedsApplying = true;
                billingAddrNeedsApplying = true;

                applyShippingAddress();
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

    /** gets shipping address from user's entries */
    private ShippingAddress getShippingAddress() {
        ShippingAddress fakeShippingAddress = new ShippingAddress();
        fakeShippingAddress.setDeliveryFirstName("Diana");
        fakeShippingAddress.setDeliveryLastName("Sutlief");
        fakeShippingAddress.setDeliveryAddress1("16041 27th Ave NE");
        fakeShippingAddress.setDeliveryCity("Shoreline");
        fakeShippingAddress.setDeliveryState("WA");
        fakeShippingAddress.setDeliveryZipCode("98155");
        fakeShippingAddress.setDeliveryPhone("206-362-8024");
        fakeShippingAddress.setEmailAddress("diana.sutlief@staples.com");
        fakeShippingAddress.setReenterEmailAddress("diana.sutlief@staples.com");
        return fakeShippingAddress;
    }

    /** gets billing address from user's entries */
    private BillingAddress getBillingAddress() {
        if (useShipAddrAsBillingAddrSwitch.isChecked()) {
            return new BillingAddress(getShippingAddress());
        } else {
            BillingAddress fakeBillingAddress = new BillingAddress();
            fakeBillingAddress.setBillingFirstName("Diana");
            fakeBillingAddress.setBillingLastName("Sutlief");
            fakeBillingAddress.setBillingAddress1("16041 27th Ave NE");
            fakeBillingAddress.setBillingCity("Shoreline");
            fakeBillingAddress.setBillingState("WA");
            fakeBillingAddress.setBillingZipCode("98155");
            fakeBillingAddress.setBillingPhone("206-362-8024");
            return fakeBillingAddress;
        }
    }

    /** gets billing address from user's entries */
    private PaymentMethod getPaymentMethod() {
        PaymentMethod fakePaymentMethod = new PaymentMethod();
        fakePaymentMethod.setCardType("Visa");
        fakePaymentMethod.setCardVerificationCode("123");
        fakePaymentMethod.setCardNumber("4111111111111111 but need to encrypt!!!!!!!!!!");
        fakePaymentMethod.setCardExpirationMonth("12");
        fakePaymentMethod.setCardExpirationYear("2020");
        return fakePaymentMethod;
    }
    
    private void applyShippingAddress() {
        ShippingAddress shippingAddress = getShippingAddress();

        // add shipping address to cart
        if (shippingAddress != null) {
            showProgressIndicator();
            secureApi.addShippingAddressToCart(shippingAddress, RECOMMENDATION, STORE_ID, LOCALE, CLIENT_ID,
                    new Callback<AddressValidationAlert>() {

                        @Override
                        public void success(AddressValidationAlert precheckoutResponse, Response response) {
                            String validationAlert = precheckoutResponse.getAddressValidationAlert();

                            if (validationAlert != null) {
                                Toast.makeText(activity, "Address alert: " + validationAlert, Toast.LENGTH_SHORT).show();
                            } else {
                                shippingAddrNeedsApplying = false;
                            }

                            if (!shippingAddrNeedsApplying && billingAddrNeedsApplying) {
                                applyBillingAddress();
                            }

                            startPrecheckoutIfReady();
                        }

                        @Override
                        public void failure(RetrofitError retrofitError) {
                            String msg = "Address error: " + ApiError.getErrorMessage(retrofitError);
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
            secureApi.addBillingAddressToCart(billingAddress, RECOMMENDATION, STORE_ID, LOCALE, CLIENT_ID,
                    new Callback<AddressValidationAlert>() {

                        @Override
                        public void success(AddressValidationAlert precheckoutResponse, Response response) {
                            String validationAlert = precheckoutResponse.getAddressValidationAlert();

                            if (validationAlert != null) {
                                Toast.makeText(activity, "Address alert: " + validationAlert, Toast.LENGTH_SHORT).show();
                            } else {
                                billingAddrNeedsApplying = false;
                            }

                            startPrecheckoutIfReady();
                        }

                        @Override
                        public void failure(RetrofitError retrofitError) {
                            String msg = "Address error: " + ApiError.getErrorMessage(retrofitError);
                            Log.d(TAG, msg);
                            Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show();
                            hideProgressIndicator();
                        }
                    });
        }
    }

    private void startPrecheckoutIfReady() {
        if (!shippingAddrNeedsApplying && !billingAddrNeedsApplying) {
            startPrecheckout();
        } else {
            hideProgressIndicator();
        }
    }

    /** overriding to handle order submission */
    @Override
    protected void onSubmit() {
        submitPaymentMethod();
    }

    private void submitPaymentMethod() {

        final PaymentMethod paymentMethod = getPaymentMethod();

        // first add selected payment method to cart
        if (paymentMethod != null) {
            showProgressIndicator();
            secureApi.addPaymentMethodToCart(paymentMethod, RECOMMENDATION, STORE_ID, LOCALE, CLIENT_ID,
                    new Callback<PaymentMethodResponse>() {
                        @Override
                        public void success(PaymentMethodResponse paymentMethodResponse, Response response) {
                            // upon payment method success, submit the order
                            submitOrder(paymentMethod.getCardVerificationCode());
                        }

                        @Override
                        public void failure(RetrofitError retrofitError) {
                            hideProgressIndicator();
                            Toast.makeText(activity, "Payment Error: " + ApiError.getErrorMessage(retrofitError), Toast.LENGTH_SHORT).show();
                        }
                    }
            );
        } else {
            Toast.makeText(activity, R.string.payment_method_required, Toast.LENGTH_SHORT).show();
        }
    }

}
