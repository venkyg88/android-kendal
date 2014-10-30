/*
 * Copyright (c) 2014 Staples, Inc. All rights reserved.
 */

package com.staples.mobile.cfa.checkout;

import android.app.Fragment;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.staples.mobile.R;
import com.staples.mobile.cfa.LoginHelper;
import com.staples.mobile.cfa.MainActivity;
import com.staples.mobile.cfa.widget.LinearLayoutWithProgressOverlay;
import com.staples.mobile.common.access.Access;
import com.staples.mobile.common.access.easyopen.api.EasyOpenApi;
import com.staples.mobile.common.access.easyopen.model.cart.Address;
import com.staples.mobile.common.access.easyopen.model.cart.AddressDetail;
import com.staples.mobile.common.access.easyopen.model.cart.Cart;
import com.staples.mobile.common.access.easyopen.model.cart.CartContents;
import com.staples.mobile.common.access.easyopen.model.member.Member;
import com.staples.mobile.common.access.easyopen.model.member.MemberDetail;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class CheckoutFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = CheckoutFragment.class.getSimpleName();

    private static final String RECOMMENDATION = "v1";
    private static final String STORE_ID = "10001";

    private static final String CATALOG_ID = "10051";
    private static final String LOCALE = "en_US";

    private static final String ZIPCODE = "01010";
    private static final String CLIENT_ID = LoginHelper.CLIENT_ID;

    private static final int MAXFETCH = 50;

    private LinearLayoutWithProgressOverlay checkoutLayout;
    private TextView shippingAddrVw;
    private TextView paymentMethodVw;
    private TextView billingAddrVw;
    private TextView deliveryRangeVw;
    private TextView couponsRewardsVw;
    private TextView shippingChargeVw;
    private TextView taxVw;
    private TextView checkoutTotalVw;

    boolean shippingAddrResponseReceived = false;
    boolean billingAddrResponseReceived = false;
    boolean profileResponseReceived = false;

    // data returned from api
    Member member;
    Address shippingAddress;
    Address billingAddress;
    Cart cart;


    // api listeners
    AddressDetailListener shippingAddrListener;
    AddressDetailListener billingAddrListener;
    ProfileListener profileListener;
    CartListener shippingChargeListener;
    CartListener taxListener;
    CartListener cartListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        Log.d(TAG, "onCreateView()");

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

        // get api object (need secure connection
        EasyOpenApi api = Access.getInstance().getEasyOpenApi(true);

        // create api listeners
        profileListener = new ProfileListener();
        shippingAddrListener = new AddressDetailListener(true);
        billingAddrListener = new AddressDetailListener(false);
        shippingChargeListener = new CartListener(true, false);
        taxListener = new CartListener(false, true);
        cartListener = new CartListener(false, false);

        // make parallel calls for shipping address, billing address, and profile

        // query for shipping address
        api.getShippingAddress(RECOMMENDATION, STORE_ID, LOCALE, CLIENT_ID, shippingAddrListener);
        // query for billing address
        api.getBillingAddress(RECOMMENDATION, STORE_ID, LOCALE, CLIENT_ID, billingAddrListener);
        // query for profile
        api.member(RECOMMENDATION, STORE_ID, LOCALE, CLIENT_ID, profileListener);


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

        // get profile addresses and credit cards
//        ${urlContext}/${storeId}/member/profile/address
//        ${urlContext}/${storeId}/member/profile/creditcard

        // submit order
//        ${urlContext}/${storeId}/cart/confirm?locale=${locale}&client_id=${clientId}

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // hide topper
        if (getActivity() instanceof MainActivity) {
            MainActivity a = (MainActivity) getActivity();
            a.showTopper(false);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        // restore topper
        if (getActivity() instanceof MainActivity) {
            MainActivity a = (MainActivity) getActivity();
            a.showTopper(true);
        }

    }

    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.shipping_addr_add:
                Toast.makeText(getActivity(), "TBD", Toast.LENGTH_SHORT).show();
                break;
            case R.id.payment_method_add:
                Toast.makeText(getActivity(), "TBD", Toast.LENGTH_SHORT).show();
                break;
            case R.id.billing_addr_add:
                Toast.makeText(getActivity(), "TBD", Toast.LENGTH_SHORT).show();
                break;
            case R.id.checkout_submit:
                Toast.makeText(getActivity(), "TBD", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    private void proceedIfReady() {
        // if initial api calls have returned
        if (profileResponseReceived && shippingAddrResponseReceived && billingAddrResponseReceived) {

            if (member == null) {
                Toast.makeText(getActivity(), "No profile found", Toast.LENGTH_SHORT).show();
            } else {
                if (shippingAddress == null) {
                    Toast.makeText(getActivity(), "No shipping address in cart, " + member.getStoredAddressCount() + " addresses in profile", Toast.LENGTH_SHORT).show();
                }
                if (billingAddress == null) {
                    Toast.makeText(getActivity(), "No billing address in cart, " + member.getCreditCardCount() + " cards in profile", Toast.LENGTH_SHORT).show();
                }
            }

            // do precheckout if enough info to do so
            if (member != null && shippingAddress != null && billingAddress != null) {
                // get api object (need secure connection)
                EasyOpenApi api = Access.getInstance().getEasyOpenApi(true);

                Toast.makeText(getActivity(), "All good, but waiting on precheckout code", Toast.LENGTH_SHORT).show();
                hideProgressIndicator(); // do this in api response when ready

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


    /** listens for completion of view request */
    class AddressDetailListener implements Callback<AddressDetail> {

        boolean shippingListener; // true if shipping address listener, false if billing address listener

        AddressDetailListener(boolean shippingListener) {
            this.shippingListener = shippingListener;
        }

        @Override
        public void success(AddressDetail addressDetail, Response response) {

            Address address = null;
            if (addressDetail != null && addressDetail.getAddress() != null &&
                    addressDetail.getAddress().size() > 0) {
                address = addressDetail.getAddress().get(0);
            }

            if (shippingListener) {
                shippingAddrResponseReceived = true;
                CheckoutFragment.this.shippingAddress = address;
                shippingAddrVw.setText(formatAddress(address)); //"Paul Gates\n56 Frost St #1\nCambridge, MA 02140"
            } else {
                billingAddrResponseReceived = true;
                CheckoutFragment.this.billingAddress = address;
                billingAddrVw.setText(formatAddress(address));
            }
            proceedIfReady();
        }

        @Override
        public void failure(RetrofitError retrofitError) {
            if (shippingListener) {
                shippingAddrResponseReceived = true;
            } else {
                billingAddrResponseReceived = true;
            }

            String msg = "Error getting " + (shippingListener? "shipping":"billing") + " address: " + retrofitError.getMessage();
            Log.d(TAG, msg);
            Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();

            proceedIfReady();


       }
    }


    /** listens for completion of view request */
    class ProfileListener implements Callback<MemberDetail> {

        @Override
        public void success(MemberDetail memberDetail, Response response) {

            Member member = null;
            if (memberDetail != null && memberDetail.getMember() != null &&
                    memberDetail.getMember().size() > 0) {
                member = memberDetail.getMember().get(0);
            }
            CheckoutFragment.this.member = member;

            profileResponseReceived = true;

            paymentMethodVw.setText("<card logo> Card ending in 3333\n(# cards in profile: " +
                    ((member != null)? ""+member.getCreditCardCount() : "0") + ")");

            proceedIfReady();
        }

        @Override
        public void failure(RetrofitError retrofitError) {
            profileResponseReceived = true;

            String msg = "Error getting profile: " + retrofitError.getMessage();
            Log.d(TAG, msg);
            Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();

            proceedIfReady();
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
            CheckoutFragment.this.cart = cart;



            if (cartListener) {

            }

            if (taxListener) {
                taxVw.setText(""+cart.getTotalTax());
            }

            if (shippingChargeListener) {
                shippingChargeVw.setText(""+cart.getShippingCharge());
            }

//            deliveryRangeVw.setText("Oct 25-29");
//            checkoutTotalVw.setText("$99.99");

        }

        @Override
        public void failure(RetrofitError retrofitError) {

            String msg = "Error getting math story: " + retrofitError.getMessage();
            Log.d(TAG, msg);
            Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();

          //  respondToFailure("Unable to obtain cart information: " + retrofitError.getMessage());
            // note: workaround to unknown field errors is to annotate model with @JsonIgnoreProperties(ignoreUnknown = true)
        }
    }

}
