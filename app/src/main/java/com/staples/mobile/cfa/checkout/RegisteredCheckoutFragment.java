/*
 * Copyright (c) 2014 Staples, Inc. All rights reserved.
 */

package com.staples.mobile.cfa.checkout;

import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.staples.mobile.R;
import com.staples.mobile.cfa.login.LoginHelper;
import com.staples.mobile.common.access.Access;
import com.staples.mobile.common.access.easyopen.model.ApiError;
import com.staples.mobile.common.access.easyopen.model.cart.Address;
import com.staples.mobile.common.access.easyopen.model.cart.AddressDetail;
import com.staples.mobile.common.access.easyopen.model.cart.BillingAddress;
import com.staples.mobile.common.access.easyopen.model.cart.PaymentMethod;
import com.staples.mobile.common.access.easyopen.model.cart.PaymentMethodResponse;
import com.staples.mobile.common.access.easyopen.model.cart.ShippingAddress;
import com.staples.mobile.common.access.easyopen.model.checkout.AddressValidationAlert;
import com.staples.mobile.common.access.easyopen.model.member.CCDetails;
import com.staples.mobile.common.access.easyopen.model.member.Member;
import com.staples.mobile.common.access.easyopen.model.member.MemberDetail;

import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class RegisteredCheckoutFragment extends CheckoutFragment implements View.OnClickListener {
    private static final String TAG = RegisteredCheckoutFragment.class.getSimpleName();

    private static final String RECOMMENDATION = "v1";
    private static final String STORE_ID = "10001";

    private static final String CATALOG_ID = "10051";
    private static final String LOCALE = "en_US";

    private static final String CLIENT_ID = LoginHelper.CLIENT_ID;

    private static final int MAXFETCH = 50;


    private TextView shippingAddrVw;
    private TextView paymentMethodVw;
    private TextView billingAddrVw;

    private boolean shippingAddrResponseReceived;
    private boolean billingAddrResponseReceived;
    private boolean profileCcResponseReceived;
    private boolean profileAddrResponseReceived;
    private boolean shippingAddrAddToCartResponseReceived;
    private boolean billingAddrAddToCartResponseReceived;


    // data returned from api
    private List<com.staples.mobile.common.access.easyopen.model.member.Address> profileAddresses;
    private List<CCDetails> profileCreditCards;
    private Address shippingAddress;
    private Address billingAddress;


    // payment method associated with the order
    CCDetails selectedPaymentMethod;

    // api listeners
    AddressDetailListener shippingAddrListener;
    AddressDetailListener billingAddrListener;
    ProfileListener profileCcListener;
    ProfileListener profileAddrListener;
    AddAddressListener addShippingAddrListener;
    AddAddressListener addBillingAddrListener;

    /** override this to specify layout for entry area */
    @Override
    protected int getEntryLayoutId() {
        return R.layout.checkout_registered_entry;
    }

    /** override this for variation on entry area */
    @Override
    protected void initEntryArea(View view) {

        // init views
        shippingAddrVw = (TextView) view.findViewById(R.id.checkout_shipping_addr);
        paymentMethodVw = (TextView) view.findViewById(R.id.checkout_payment_method);
        billingAddrVw = (TextView) view.findViewById(R.id.checkout_billing_addr);

        // Set click listeners
        view.findViewById(R.id.shipping_addr_add).setOnClickListener(this);
        view.findViewById(R.id.payment_method_add).setOnClickListener(this);
        view.findViewById(R.id.billing_addr_add).setOnClickListener(this);

        // get api objects
        secureApi = Access.getInstance().getEasyOpenApi(true);

        // create api listeners
        profileAddrListener = new ProfileListener(true);
        profileCcListener = new ProfileListener(false);
        shippingAddrListener = new AddressDetailListener(true);
        billingAddrListener = new AddressDetailListener(false);
        addShippingAddrListener = new AddAddressListener(true);
        addBillingAddrListener = new AddAddressListener(false);

        // initialize data prior to making api calls which will fill it
        shippingAddrResponseReceived = false;
        billingAddrResponseReceived = false;
        profileCcResponseReceived = false;
        profileAddrResponseReceived = false;
        shippingAddress = null;
        billingAddress = null;
        shippingAddrAddToCartResponseReceived = true; // init to true until we know call will be needed
        billingAddrAddToCartResponseReceived = true; // init to true until we know call will be needed

        // Set initial visibility
        showProgressIndicator();

        // make parallel calls for shipping address, billing address, and profile info

        // query for shipping address
        secureApi.getShippingAddress(RECOMMENDATION, STORE_ID, LOCALE, CLIENT_ID, shippingAddrListener);
        // query for billing address
        secureApi.getBillingAddress(RECOMMENDATION, STORE_ID, LOCALE, CLIENT_ID, billingAddrListener);
        // query for profile credit cards
        secureApi.getMemberCreditCardDetails(RECOMMENDATION, STORE_ID, LOCALE, CLIENT_ID, profileCcListener);
        // query for profile addresses
        secureApi.getMemberAddress(RECOMMENDATION, STORE_ID, LOCALE, CLIENT_ID, profileAddrListener);

    }

    /** overriding to handle order submission */
    @Override
    protected void onSubmit() {
        submitPaymentMethod(null); // cid not necessary for registered users
    }

    private void submitPaymentMethod(final String cid) {

        // first add selected payment method to cart
        if (selectedPaymentMethod != null) {
            showProgressIndicator();

            PaymentMethod paymentMethod = new PaymentMethod(selectedPaymentMethod);
            paymentMethod.setCardVerificationCode(cid);
            secureApi.addPaymentMethodToCart(paymentMethod, RECOMMENDATION, STORE_ID, LOCALE, CLIENT_ID,
                    new Callback<PaymentMethodResponse>() {
                        @Override
                        public void success(PaymentMethodResponse paymentMethodResponse, Response response) {
                            // upon payment method success, submit the order
                            submitOrder(cid);
                        }

                        @Override
                        public void failure(RetrofitError retrofitError) {
                            hideProgressIndicator();
                            Toast.makeText(activity, "Payment Error: " + ApiError.getErrorMessage(retrofitError), Toast.LENGTH_SHORT).show();
                        }
                    }
            );
        }
    }



    @Override
    public void onClick(View view) {
        super.onClick(view);
        switch(view.getId()) {
            case R.id.shipping_addr_add:
                Toast.makeText(activity, "TBD", Toast.LENGTH_SHORT).show();
                break;
            case R.id.payment_method_add:
                Toast.makeText(activity, "TBD", Toast.LENGTH_SHORT).show();
                break;
            case R.id.billing_addr_add:
                Toast.makeText(activity, "TBD", Toast.LENGTH_SHORT).show();
                break;
            case R.id.co_submission_layout:
                submitPaymentMethod(null); // cid not necessary for registered users
                break;
        }
    }


    private boolean isFirstApiPassComplete() {
        return (profileAddrResponseReceived && profileCcResponseReceived &&
                shippingAddrResponseReceived && billingAddrResponseReceived);
    }


    private void startSecondWaveIfReady() {

        // if first wave of api calls have returned
        if (isFirstApiPassComplete()) {
            applyAddressesAndPayment();
        }
    }

    protected void startPrecheckoutIfReady() {
        if (billingAddrAddToCartResponseReceived && shippingAddrAddToCartResponseReceived) {
            if (shippingAddress != null && billingAddress != null) {
                startPrecheckout();
            } else {
                hideProgressIndicator();
            }
        }
    }

    protected void applyAddressesAndPayment() {

        // if profile addresses available
        if (profileAddresses != null && profileAddresses.size() > 0) {
            com.staples.mobile.common.access.easyopen.model.member.Address profileAddress = profileAddresses.get(0);
            Address address = new Address(profileAddress);

            // if cart shipping address null, but address available from profile then add it to cart
            if (shippingAddress == null) {
                shippingAddress = address;
                shippingAddrVw.setText(formatAddress(shippingAddress));
                // add profile address to cart
                shippingAddrAddToCartResponseReceived = false;
                secureApi.addShippingAddressToCart(new ShippingAddress(profileAddress),
                        RECOMMENDATION, STORE_ID, LOCALE, CLIENT_ID, addShippingAddrListener);
            }

            // if cart billing address null, but address available from profile then add it to cart
            if (billingAddress == null) {
                billingAddress = address;
                billingAddrVw.setText(formatAddress(billingAddress));
                // add profile address to cart
                billingAddrAddToCartResponseReceived = false;
                secureApi.addBillingAddressToCart(new BillingAddress(billingAddress),
                        RECOMMENDATION, STORE_ID, LOCALE, CLIENT_ID, addBillingAddrListener);
            }
        } else {
            Toast.makeText(activity, "No profile addresses available", Toast.LENGTH_SHORT).show();
        }


        // if CC available from profile
        if (profileCreditCards != null && profileCreditCards.size() > 0) {
            CCDetails cc = profileCreditCards.get(0);
            if (!TextUtils.isEmpty(cc.getCardNumber()) && cc.getCardNumber().length() >= 4) {
                paymentMethodVw.setText(cc.getCardType() + "  ending in " +
                        cc.getCardNumber().substring(cc.getCardNumber().length() - 4));
                selectedPaymentMethod = cc;
            } else {
                Toast.makeText(activity, "Credit card number is blank", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(activity, "No payment methods available", Toast.LENGTH_SHORT).show();
        }

        // if done because of issues above or addresses already part of order, start precheck if possible
        startPrecheckoutIfReady();
    }

    private String formatAddress(Address address) {
        StringBuilder b = new StringBuilder();
        if (address != null) {
            if (!TextUtils.isEmpty(address.getFirstName())) {
                b.append(address.getFirstName());
                if (!TextUtils.isEmpty(address.getLastName())) {
                    b.append(" ").append(address.getLastName());
                }
                b.append("\n");
            }
            if (!TextUtils.isEmpty(address.getOrganizationName())) {
                b.append(address.getOrganizationName()).append("\n");
            }
            b.append(address.getAddress1() + "\n");
            if (!TextUtils.isEmpty(address.getAddress2())) {
                b.append(address.getAddress2()).append("\n");
            }
            if (!TextUtils.isEmpty(address.getCity())) {
                b.append(address.getCity());
                if (!TextUtils.isEmpty(address.getState())) {
                    b.append(" ").append(address.getState());
                }
                if (!TextUtils.isEmpty(address.getZipCode())) {
                    b.append(" ").append(address.getZipCode());
                }
                b.append("\n");
            }
        }
        return b.toString();
    }


    /************* api listeners ************/


    /** listens for completion of cart address request */
    class AddressDetailListener implements Callback<AddressDetail> {

        boolean listeningForShippingAddr; // true if shipping address listener, false if billing address listener

        AddressDetailListener(boolean listeningForShippingAddr) {
            this.listeningForShippingAddr = listeningForShippingAddr;
        }

        @Override
        public void success(AddressDetail addressDetail, Response response) {

            Address address = null;
            if (addressDetail != null && addressDetail.getAddress() != null &&
                    addressDetail.getAddress().size() > 0) {
                address = addressDetail.getAddress().get(0);
            }

            if (listeningForShippingAddr) {
                shippingAddrResponseReceived = true;
                RegisteredCheckoutFragment.this.shippingAddress = address;
                shippingAddrVw.setText(formatAddress(address)); //"Paul Gates\n56 Frost St #1\nCambridge, MA 02140"
            } else {
                billingAddrResponseReceived = true;
                RegisteredCheckoutFragment.this.billingAddress = address;
                billingAddrVw.setText(formatAddress(address));
            }
            startSecondWaveIfReady();
        }

        @Override
        public void failure(RetrofitError retrofitError) {

            boolean normalAddrNotAvailResponse = (retrofitError.getResponse() != null &&
                    retrofitError.getResponse().getStatus() == 400);


            if (listeningForShippingAddr) {

                // query for profile addresses
                shippingAddrResponseReceived = true;
            } else {
                billingAddrResponseReceived = true;
            }

            if (!normalAddrNotAvailResponse) {
                String msg = "Error getting " + (listeningForShippingAddr ? "shipping" : "billing") + " address: " + ApiError.getErrorMessage(retrofitError);
                Log.d(TAG, msg);
                Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show();
            }

            startSecondWaveIfReady();
       }
    }


    /** listens for completion of profile request */
    class ProfileListener implements Callback<MemberDetail> {

        boolean listeningForAddresses; // true if profile address listener, false if profile CC listener

        ProfileListener(boolean listeningForAddresses) {
            this.listeningForAddresses = listeningForAddresses;
        }

        @Override
        public void success(MemberDetail memberDetail, Response response) {

            Member member = null;
            if (memberDetail != null && memberDetail.getMember() != null &&
                    memberDetail.getMember().size() > 0) {
                member = memberDetail.getMember().get(0);
            }


            if (listeningForAddresses) {
                if (member != null) {
                    profileAddresses = member.getAddress();
                }
                profileAddrResponseReceived = true;
            } else {
                if (member != null) {
                    profileCreditCards = member.getCreditCard();
                }
                profileCcResponseReceived = true;
            }

            startSecondWaveIfReady();
        }

        @Override
        public void failure(RetrofitError retrofitError) {

            if (listeningForAddresses) {
                profileAddrResponseReceived = true;
            } else {
                profileCcResponseReceived = true;
            }

            String msg = "Error getting profile: " + ApiError.getErrorMessage(retrofitError);
            Log.d(TAG, msg);
            Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show();

            startSecondWaveIfReady();
        }
    }


    /** listens for completion of precheckout */
    class AddAddressListener implements Callback<AddressValidationAlert> {

        boolean listeningForShippingAddr; // true if shipping address listener, false if billing address listener

        AddAddressListener(boolean listeningForShippingAddr) {
            this.listeningForShippingAddr = listeningForShippingAddr;
        }

        @Override
        public void success(AddressValidationAlert precheckoutResponse, Response response) {
            String validationAlert = precheckoutResponse.getAddressValidationAlert();

            if (validationAlert != null) {
                Toast.makeText(activity, "Address alert: " + validationAlert, Toast.LENGTH_SHORT).show();
            }

            if (listeningForShippingAddr) {
                shippingAddrAddToCartResponseReceived = true;
            } else {
                billingAddrAddToCartResponseReceived = true;
            }
            startPrecheckoutIfReady();
        }

        @Override
        public void failure(RetrofitError retrofitError) {

            if (listeningForShippingAddr) {
                shippingAddrAddToCartResponseReceived = true;
            } else {
                billingAddrAddToCartResponseReceived = true;
            }

            String msg = "Address error: " + ApiError.getErrorMessage(retrofitError);
            Log.d(TAG, msg);
            Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show();
            hideProgressIndicator();
        }
    }

}
