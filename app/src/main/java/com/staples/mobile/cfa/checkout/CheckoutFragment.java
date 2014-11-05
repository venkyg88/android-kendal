/*
 * Copyright (c) 2014 Staples, Inc. All rights reserved.
 */

package com.staples.mobile.cfa.checkout;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.staples.mobile.R;
import com.staples.mobile.cfa.login.LoginHelper;
import com.staples.mobile.cfa.MainActivity;
import com.staples.mobile.cfa.widget.LinearLayoutWithProgressOverlay;
import com.staples.mobile.common.access.Access;
import com.staples.mobile.common.access.easyopen.api.EasyOpenApi;
import com.staples.mobile.common.access.easyopen.model.ApiError;
import com.staples.mobile.common.access.easyopen.model.cart.Address;
import com.staples.mobile.common.access.easyopen.model.cart.AddressDetail;
import com.staples.mobile.common.access.easyopen.model.cart.BillingAddress;
import com.staples.mobile.common.access.easyopen.model.cart.Cart;
import com.staples.mobile.common.access.easyopen.model.cart.CartContents;
//import com.staples.mobile.common.access.easyopen.model.cart.OrderStatus;
//import com.staples.mobile.common.access.easyopen.model.cart.OrderStatusContents;
import com.staples.mobile.common.access.easyopen.model.cart.PaymentMethod;
import com.staples.mobile.common.access.easyopen.model.cart.PaymentMethodResponse;
import com.staples.mobile.common.access.easyopen.model.cart.ShippingAddress;
import com.staples.mobile.common.access.easyopen.model.checkout.SubmitOrderRequest;
import com.staples.mobile.common.access.easyopen.model.checkout.SubmitOrderResponse;
import com.staples.mobile.common.access.easyopen.model.member.CCDetails;
import com.staples.mobile.common.access.easyopen.model.member.Member;
import com.staples.mobile.common.access.easyopen.model.member.MemberDetail;
import com.staples.mobile.common.access.easyopen.model.checkout.AddressValidationAlert;

import java.text.NumberFormat;
import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class CheckoutFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = CheckoutFragment.class.getSimpleName();

    private static final String RECOMMENDATION = "v1";
    private static final String STORE_ID = "10001";

    private static final String CATALOG_ID = "10051";
    private static final String LOCALE = "en_US";

    private static final String CLIENT_ID = LoginHelper.CLIENT_ID;

    private static final int MAXFETCH = 50;


    public static final String BUNDLE_PARAM_DELIVERYRANGE = "deliveryRange";
    public static final String BUNDLE_PARAM_PRETAXSUBTOTAL = "preTaxSubtotal";

    NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();

    // saving around Activity object since getActivity() returns null after user navigates away from
    // fragment, but api call may still be returning
    private Activity activity;

    private LinearLayoutWithProgressOverlay checkoutLayout;
    private TextView shippingAddrVw;
    private TextView paymentMethodVw;
    private TextView billingAddrVw;
    private TextView deliveryRangeVw;
    private TextView couponsRewardsVw;
    private TextView shippingChargeVw;
    private TextView taxVw;
    private TextView checkoutTotalVw;

    boolean shippingAddrResponseReceived;
    boolean billingAddrResponseReceived;
    boolean profileCcResponseReceived;
    boolean profileAddrResponseReceived;
    boolean shippingAddrAddToCartResponseReceived;
    boolean billingAddrAddToCartResponseReceived;

    // api objects
    EasyOpenApi secureApi;

    // data returned from api
    List<com.staples.mobile.common.access.easyopen.model.member.Address> profileAddresses;
    List<CCDetails> profileCreditCards;
    Address shippingAddress;
    Address billingAddress;
    Float tax;


    // data initialized from cart drawer
    Float pretaxSubtotal;

    // payment method associated with the order
    CCDetails selectedPaymentMethod;

    // api listeners
    AddressDetailListener shippingAddrListener;
    AddressDetailListener billingAddrListener;
    ProfileListener profileCcListener;
    ProfileListener profileAddrListener;
    CartListener shippingChargeListener;
    CartListener taxListener;
//    CartListener cartListener;
    PrecheckoutListener addShippingAddrListener;
    PrecheckoutListener addBillingAddrListener;
    PrecheckoutListener precheckoutListener;
//    OrderStatusListener orderStatusListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        Log.d(TAG, "onCreateView()");

        activity = getActivity();

        // inflate and get child views
        View view = inflater.inflate(R.layout.checkout_fragment, container, false);
        checkoutLayout = (LinearLayoutWithProgressOverlay) view.findViewById(R.id.checkout);
        checkoutLayout.setCartProgressOverlay(view.findViewById(R.id.checkout_progress_overlay));
        shippingAddrVw = (TextView) view.findViewById(R.id.checkout_shipping_addr);
        paymentMethodVw = (TextView) view.findViewById(R.id.checkout_payment_method);
        billingAddrVw = (TextView) view.findViewById(R.id.checkout_billing_addr);
        deliveryRangeVw = (TextView) view.findViewById(R.id.checkout_delivery_range);
        couponsRewardsVw = (TextView) view.findViewById(R.id.checkout_coupons_rewards);
        shippingChargeVw = (TextView) view.findViewById(R.id.checkout_shipping);
        taxVw = (TextView) view.findViewById(R.id.checkout_tax);
        checkoutTotalVw = (TextView) view.findViewById(R.id.checkout_order_total);

        // Set click listeners
        view.findViewById(R.id.shipping_addr_add).setOnClickListener(this);
        view.findViewById(R.id.payment_method_add).setOnClickListener(this);
        view.findViewById(R.id.billing_addr_add).setOnClickListener(this);
        view.findViewById(R.id.checkout_submit).setOnClickListener(this);

        // Set initial visibility
        showProgressIndicator();

        // get order info from bundle
        Bundle checkoutBundle = this.getArguments();
        deliveryRangeVw.setText(checkoutBundle.getString(BUNDLE_PARAM_DELIVERYRANGE));
        pretaxSubtotal = checkoutBundle.getFloat(BUNDLE_PARAM_PRETAXSUBTOTAL);


        // get api objects
        secureApi = Access.getInstance().getEasyOpenApi(true);

        // create api listeners
        profileAddrListener = new ProfileListener(true);
        profileCcListener = new ProfileListener(false);
        shippingAddrListener = new AddressDetailListener(true);
        billingAddrListener = new AddressDetailListener(false);
        shippingChargeListener = new CartListener(true, false);
        taxListener = new CartListener(false, true);
//        cartListener = new CartListener(false, false);
        addShippingAddrListener = new PrecheckoutListener(true, false);
        addBillingAddrListener = new PrecheckoutListener(false, false);
        precheckoutListener = new PrecheckoutListener(true, true);
//        orderStatusListener = new OrderStatusListener();


        // initialize data prior to making api calls which will fill it
        shippingAddrResponseReceived = false;
        billingAddrResponseReceived = false;
        profileCcResponseReceived = false;
        profileAddrResponseReceived = false;
        shippingAddrAddToCartResponseReceived = true; // init to true until we know call will be needed
        billingAddrAddToCartResponseReceived = true; // init to true until we know call will be needed
        tax = null;
        shippingAddress = null;
        billingAddress = null;

        // make parallel calls for shipping address, billing address, and profile info

        // query for shipping address
        secureApi.getShippingAddress(RECOMMENDATION, STORE_ID, LOCALE, CLIENT_ID, shippingAddrListener);
        // query for billing address
        secureApi.getBillingAddress(RECOMMENDATION, STORE_ID, LOCALE, CLIENT_ID, billingAddrListener);
        // query for profile credit cards
        secureApi.getMemberCreditCardDetails(RECOMMENDATION, STORE_ID, LOCALE, CLIENT_ID, profileCcListener);
        // query for profile addresses
        secureApi.getMemberAddress(RECOMMENDATION, STORE_ID, LOCALE, CLIENT_ID, profileAddrListener);


        // precheckout - when does this happen ???
        // This service is a required part of the checkout flow. It checks the availability of items
        // in the cart, inventory, qualifying the coupons, calculating the tax for the order and
        // validation of the shipping address. If the shipping address needs correction, the response
        // will include the suggested address. The returned address is only a suggestion and is not
        // a requirement to be used.
//        ${urlContext}/${storeId}/cart/precheckout?locale=${locale}&client_id=${clientId}

        // get/add order's shipping address
//        ${urlContext}/${storeId}/cart/address/shipping?locale=${locale}&client_id=${clientId}

        // get/add order's billing address
//        ${urlContext}/${storeId}/cart/address/billing?locale=${locale}&client_id=${clientId}

        // get/add order's payment method
//        ${urlContext}/${storeId}/cart/payment?locale=${locale}&client_id=${clientId}

        // submit order
//        ${urlContext}/${storeId}/cart/confirm?locale=${locale}&client_id=${clientId}

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // hide topper
        if (activity instanceof MainActivity) {
            MainActivity a = (MainActivity) activity;
            a.showTopper(false);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        // restore topper
        if (activity instanceof MainActivity) {
            MainActivity a = (MainActivity) activity;
            a.showTopper(true);
        }

    }

    @Override
    public void onClick(View view) {
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
            case R.id.checkout_submit:
                submitPaymentMethod(null); // cid not necessary for registered users
                break;
        }
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

    private void submitOrder(String cid) {
        // upon payment method success, submit the order
        SubmitOrderRequest submitOrderRequest = new SubmitOrderRequest();
        submitOrderRequest.setCardVerificationCode(cid);
        secureApi.submitOrder(submitOrderRequest, RECOMMENDATION, STORE_ID, LOCALE, CLIENT_ID,
                new Callback<SubmitOrderResponse>() {

                    @Override
                    public void success(SubmitOrderResponse submitOrderResponse, Response response) {
                        hideProgressIndicator();
                        Toast.makeText(activity, "SUCCESS! Order: " +
                                submitOrderResponse.getStaplesOrderNumber(), Toast.LENGTH_SHORT).show();

                        // show confirmation page and refresh cart
                        ((MainActivity)activity).selectOrderConfirmation(
                                submitOrderResponse.getOrderId(),
                                submitOrderResponse.getStaplesOrderNumber());

                        //success: qa21, diana, order # 9707186646, # 9707187319
                    }

                    @Override
                    public void failure(RetrofitError retrofitError) {
                        hideProgressIndicator();
                        Toast.makeText(activity, "Submission Error: " + ApiError.getErrorMessage(retrofitError), Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }


    private boolean isFirstApiPassComplete() {
        return (profileAddrResponseReceived && profileCcResponseReceived &&
                shippingAddrResponseReceived && billingAddrResponseReceived);
    }

    private boolean isSecondApiPassComplete() {
        return (billingAddrAddToCartResponseReceived && shippingAddrAddToCartResponseReceived);
    }



    private void startSecondWaveIfReady() {

        // if first wave of api calls have returned
        if (isFirstApiPassComplete()) {

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
            if (isSecondApiPassComplete()) {
                startPrecheckoutIfReady();
            }

        }
    }

    private void startPrecheckoutIfReady() {

        // if first and second waves of api calls have returned
        if (isFirstApiPassComplete() && isSecondApiPassComplete()) {

            if (shippingAddress != null && billingAddress != null) {
                secureApi.precheckout(RECOMMENDATION, STORE_ID, LOCALE, CLIENT_ID, precheckoutListener);
            } else {
                hideProgressIndicator();
            }
        }
    }

    private void showProgressIndicator() {
        checkoutLayout.getProgressIndicator().showProgressIndicator();
    }

    private void hideProgressIndicator() {
        checkoutLayout.getProgressIndicator().hideProgressIndicator();
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

    // Retrofit callbacks

//    @Override
//    public void success(SkuDetails sku, Response response) {
//    }
//
//    @Override
//    public void failure(RetrofitError retrofitError) {
//        Log.d(TAG, "Failure callback " + retrofitError);
//    }
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
                CheckoutFragment.this.shippingAddress = address;
                shippingAddrVw.setText(formatAddress(address)); //"Paul Gates\n56 Frost St #1\nCambridge, MA 02140"
            } else {
                billingAddrResponseReceived = true;
                CheckoutFragment.this.billingAddress = address;
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


    /** listens for completion of view request */
    class CartListener implements Callback<CartContents> {

        boolean shippingChargeListener; // true if shipping charge listener
        boolean taxListener; // true if tax listener
        boolean cartListener; // true if cart listener

        CartListener(boolean shippingChargeListener, boolean taxListener) {
            this.shippingChargeListener = shippingChargeListener;
            this.taxListener = taxListener && !shippingChargeListener;
            this.cartListener = (!shippingChargeListener && !taxListener);
        }

        @Override
        public void success(CartContents cartContents, Response response) {

            Cart cart = null;
            if (cartContents != null && cartContents.getCart() != null &&
                    cartContents.getCart().size() > 0) {
                cart = cartContents.getCart().get(0);
            }

            if (taxListener) {
                tax = cart.getTotalTax();
                taxVw.setText(currencyFormat.format(tax));
                updateGrandTotal();
            }

            if (shippingChargeListener) {
                shippingChargeVw.setText(""+cart.getShippingCharge());
            }


            hideProgressIndicator();
        }

        private void updateGrandTotal() {
            if (pretaxSubtotal != null && tax != null) {
                checkoutTotalVw.setText(currencyFormat.format(pretaxSubtotal + tax));
            } else {
                checkoutTotalVw.setText("");
            }
        }

        @Override
        public void failure(RetrofitError retrofitError) {

            String msg = "Error getting math story: " + ApiError.getErrorMessage(retrofitError);
            Log.d(TAG, msg);
            Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show();

          //  respondToFailure("Unable to obtain cart information: " + ApiError.getErrorMessage(retrofitError));
            // note: workaround to unknown field errors is to annotate model with @JsonIgnoreProperties(ignoreUnknown = true)
        }
    }


    /** listens for completion of precheckout */
    class PrecheckoutListener implements Callback<AddressValidationAlert> {

        boolean listeningForShippingAddr; // true if shipping address listener, false if billing address listener
        boolean listeningForPrecheckout; // true if precheckout listener

        PrecheckoutListener(boolean listeningForShippingAddr, boolean listeningForPrecheckout) {
            this.listeningForShippingAddr = listeningForShippingAddr;
            this.listeningForPrecheckout = listeningForPrecheckout;
        }

        @Override
        public void success(AddressValidationAlert precheckoutResponse, Response response) {
            String validationAlert = precheckoutResponse.getAddressValidationAlert();

            if (listeningForPrecheckout) {
                // get tax and shipping charge
                secureApi.getTax(RECOMMENDATION, STORE_ID, LOCALE, CLIENT_ID, taxListener);
                secureApi.getShippingCharge(RECOMMENDATION, STORE_ID, LOCALE, CLIENT_ID, shippingChargeListener);

                Toast.makeText(activity, "precheckout succeeded", Toast.LENGTH_SHORT).show();
            } else {
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
        }

        @Override
        public void failure(RetrofitError retrofitError) {

            if (listeningForShippingAddr) {
                shippingAddrAddToCartResponseReceived = true;
            } else {
                billingAddrAddToCartResponseReceived = true;
            }

            String msg;
            if (listeningForPrecheckout) {
                msg = "Precheckout error: " + ApiError.getErrorMessage(retrofitError);
            } else {
                msg = "Address error: " + ApiError.getErrorMessage(retrofitError);
            }
            Log.d(TAG, msg);
            Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show();
            hideProgressIndicator();
        }
    }

}
