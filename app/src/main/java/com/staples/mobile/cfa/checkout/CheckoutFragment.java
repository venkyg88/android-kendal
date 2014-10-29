/*
 * Copyright (c) 2014 Staples, Inc. All rights reserved.
 */

package com.staples.mobile.cfa.checkout;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.staples.mobile.R;
import com.staples.mobile.cfa.MainActivity;
import com.staples.mobile.cfa.cart.LinearLayoutWithProgressOverlay;
import com.staples.mobile.common.access.Access;
import com.staples.mobile.common.access.easyopen.api.EasyOpenApi;
import com.staples.mobile.common.access.easyopen.model.cart.Cart;
import com.staples.mobile.common.access.easyopen.model.cart.CartContents;

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

    private static final String ZIPCODE = "01010";
    private static final String CLIENT_ID = "N6CA89Ti14E6PAbGTr5xsCJ2IGaHzGwS";

    private static final int MAXFETCH = 50;

    private LinearLayoutWithProgressOverlay checkoutLayout;
    private ProgressBar progressBar;
    private TextView shippingAddrVw;
    private TextView paymentMethodVw;
    private TextView billingAddrVw;
    private TextView deliveryRangeVw;
    private TextView couponsRewardsVw;
    private TextView shippingVw;
    private TextView taxVw;
    private TextView checkoutTotalVw;

    boolean cartInfoLoaded = false;
    boolean profileInfoLoaded = false;

    Cart cart;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        Log.d(TAG, "onCreateView()");

        // inflate and get child views
        View view = inflater.inflate(R.layout.checkout_fragment, container, false);
        checkoutLayout = (LinearLayoutWithProgressOverlay) view.findViewById(R.id.checkout);
        checkoutLayout.setCartProgressOverlay(view.findViewById(R.id.checkout_progress_overlay));
//        progressBar = (ProgressBar) view.findViewById(R.id.checkout_progress_bar);
        shippingAddrVw = (TextView) view.findViewById(R.id.checkout_shipping_addr);
        paymentMethodVw = (TextView) view.findViewById(R.id.checkout_payment_method);
        billingAddrVw = (TextView) view.findViewById(R.id.checkout_billing_addr);
        deliveryRangeVw = (TextView) view.findViewById(R.id.checkout_delivery_range);
        couponsRewardsVw = (TextView) view.findViewById(R.id.checkout_coupons_rewards);
        shippingVw = (TextView) view.findViewById(R.id.checkout_shipping);
        taxVw = (TextView) view.findViewById(R.id.checkout_tax);
        checkoutTotalVw = (TextView) view.findViewById(R.id.checkout_order_total);


        // Set listeners
        view.findViewById(R.id.shipping_addr_add).setOnClickListener(this);
        view.findViewById(R.id.payment_method_add).setOnClickListener(this);
        view.findViewById(R.id.billing_addr_add).setOnClickListener(this);
        view.findViewById(R.id.checkout_submit).setOnClickListener(this);

        // Set initial visibility
        showProgressIndicator();

        // get api object (need secure connection
        EasyOpenApi api = Access.getInstance().getEasyOpenApi(true);

        // call api for info
//        api.viewProfile(RECOMMENDATION, STORE_ID, CATALOG_ID, LOCALE,
//                                                              ZIPCODE, CLIENT_ID, null, MAXFETCH, this);

        // todo: remove the following:
        // fake the profile info for now
        profileInfoLoaded = true;
        shippingAddrVw.setText("Paul Gates\n56 Frost St #1\nCambridge, MA 02140");
        paymentMethodVw.setText("<card logo> Card ending in 3333");
        billingAddrVw.setText("Paul Gates\n56 Frost St #1\nCambridge, MA 02140");

        // get the cart info

        // precheckout - when does this happen ???
        // This service is a required part of the checkout flow. It checks the availability of items
        // in the cart, inventory, qualifying the coupons, calculating the tax for the order and
        // validation of the shipping address. If the shipping address needs correction, the response
        // will include the suggested address. The returned address is only a suggestion and is not
        // a requirement to be used.
//        ${urlContext}/${storeId}/cart/precheckout?locale=${locale}&client_id=${clientId}

        // get order's tax, shipping
//        ${urlContext}/${storeId}/cart/tax
//        ${urlContext}/${storeId}/cart/shipping/charge

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

    private void showProgressIndicator() {
        checkoutLayout.getProgressIndicator().showProgressIndicator();
//        checkoutLayout.setVisibility(View.GONE);
//        progressBar.setVisibility(View.VISIBLE);
    }

    private void hideProgressIndicator() {
        checkoutLayout.getProgressIndicator().hideProgressIndicator();
//        checkoutLayout.setVisibility(View.VISIBLE);
//        progressBar.setVisibility(View.GONE);
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
    class ViewCartListener implements Callback<CartContents> {

        @Override
        public void success(CartContents cartContents, Response response) {

            // get data from cartContent request
            List<Cart> cartCollection = cartContents.getCart();
            if (cartCollection != null && cartCollection.size() > 0) {
                cart = cartCollection.get(0);
//cart.get
                deliveryRangeVw.setText("Oct 25-29");
                shippingVw.setText(cart.getDelivery());
                taxVw.setText("$0.99");
                checkoutTotalVw.setText("$99.99");

            }

            cartInfoLoaded = true;

            // if all info loaded, remove progress indicator
            if (cartInfoLoaded && profileInfoLoaded) {
                hideProgressIndicator();
            }

        }

        @Override
        public void failure(RetrofitError retrofitError) {
//            respondToFailure("Unable to obtain cart information: " + retrofitError.getMessage());
            // note: workaround to unknown field errors is to annotate model with @JsonIgnoreProperties(ignoreUnknown = true)
        }
    }


}
