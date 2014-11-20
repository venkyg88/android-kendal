/*
 * Copyright (c) 2014 Staples, Inc. All rights reserved.
 */

package com.staples.mobile.cfa.checkout;

import android.app.Activity;
import android.app.Fragment;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.staples.mobile.R;
import com.staples.mobile.cfa.login.LoginHelper;
import com.staples.mobile.cfa.MainActivity;
import com.staples.mobile.cfa.widget.LinearLayoutWithProgressOverlay;
import com.staples.mobile.common.access.Access;
import com.staples.mobile.common.access.easyopen.api.EasyOpenApi;
import com.staples.mobile.common.access.easyopen.model.ApiError;
import com.staples.mobile.common.access.easyopen.model.cart.Cart;
import com.staples.mobile.common.access.easyopen.model.cart.CartContents;
import com.staples.mobile.common.access.easyopen.model.checkout.SubmitOrderRequest;
import com.staples.mobile.common.access.easyopen.model.checkout.SubmitOrderResponse;
import com.staples.mobile.common.access.easyopen.model.checkout.AddressValidationAlert;

import java.text.NumberFormat;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


public abstract class CheckoutFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = CheckoutFragment.class.getSimpleName();

    private static final String RECOMMENDATION = "v1";
    private static final String STORE_ID = "10001";

    private static final String CATALOG_ID = "10051";
    private static final String LOCALE = "en_US";

    private static final String CLIENT_ID = LoginHelper.CLIENT_ID;

    private static final int MAXFETCH = 50;


    public static final String BUNDLE_PARAM_DELIVERYRANGE = "deliveryRange";
    public static final String BUNDLE_PARAM_ITEMSUBTOTAL = "itemSubtotal";
    public static final String BUNDLE_PARAM_PRETAXSUBTOTAL = "preTaxSubtotal";

    NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();

    // saving around Activity object since getActivity() returns null after user navigates away from
    // fragment, but api call may still be returning
    protected MainActivity activity;

    private LinearLayoutWithProgressOverlay checkoutLayout;
    private View shippingChargeLayout;
    private View taxLayout;
    private View submissionLayout;
    private ViewGroup checkoutEntryLayout;
//    private TextView deliveryRangeVw;
    private TextView itemSubtotalVw;
    private TextView couponsRewardsVw;
    private TextView shippingChargeVw;
    private TextView taxVw;
    private TextView checkoutTotalVw;


    // api objects
    protected EasyOpenApi secureApi;

    // data returned from api
    private Float tax;


    // data initialized from cart drawer
    Float itemSubtotal;
    Float pretaxSubtotal;


    // api listeners
    CartListener shippingChargeListener;
    CartListener taxListener;
    PrecheckoutListener precheckoutListener;


    /**
     * Create a new instance of ConfirmationFragment that will be initialized
     * with the given arguments.
     */
    public static CheckoutFragment newInstance(String deliveryRange, float itemSubtotal, float preTaxSubtotal, boolean registered) {
        CheckoutFragment f = registered? new RegisteredCheckoutFragment() : new GuestCheckoutFragment();
        Bundle args = new Bundle();
        args.putString(CheckoutFragment.BUNDLE_PARAM_DELIVERYRANGE, deliveryRange);
        args.putFloat(CheckoutFragment.BUNDLE_PARAM_ITEMSUBTOTAL, itemSubtotal);
        args.putFloat(CheckoutFragment.BUNDLE_PARAM_PRETAXSUBTOTAL, preTaxSubtotal);
        f.setArguments(args);
        return f;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        Log.d(TAG, "onCreateView()");

        Resources r = getResources();

        activity = (MainActivity)getActivity();

        // inflate and get child views
        View view = inflater.inflate(R.layout.checkout_fragment, container, false);
        checkoutLayout = (LinearLayoutWithProgressOverlay) view.findViewById(R.id.checkout);
        checkoutLayout.setCartProgressOverlay(view.findViewById(R.id.checkout_progress_overlay));
        checkoutEntryLayout = (ViewGroup)view.findViewById(R.id.checkout_entry_layout);
        inflater.inflate(getEntryLayoutId(), checkoutEntryLayout); // dynamically inflate variable entry area
        shippingChargeLayout = view.findViewById(R.id.co_shipping_layout);
        submissionLayout = view.findViewById(R.id.co_submission_layout);
        taxLayout = view.findViewById(R.id.co_tax_layout);
//        deliveryRangeVw = (TextView) view.findViewById(R.id.checkout_delivery_range);
        itemSubtotalVw = (TextView) view.findViewById(R.id.checkout_item_subtotal);
        couponsRewardsVw = (TextView) view.findViewById(R.id.checkout_coupons_rewards);
        shippingChargeVw = (TextView) view.findViewById(R.id.checkout_shipping);
        taxVw = (TextView) view.findViewById(R.id.checkout_tax);
        checkoutTotalVw = (TextView) view.findViewById(R.id.checkout_order_total);

        // Set click listeners
        submissionLayout.setOnClickListener(this);

        // get order info from bundle
        Bundle checkoutBundle = this.getArguments();
        String deliveryRange = checkoutBundle.getString(BUNDLE_PARAM_DELIVERYRANGE);
        itemSubtotal = checkoutBundle.getFloat(BUNDLE_PARAM_ITEMSUBTOTAL);
        pretaxSubtotal = checkoutBundle.getFloat(BUNDLE_PARAM_PRETAXSUBTOTAL);

        // set the item subtotal
        itemSubtotalVw.setText(currencyFormat.format(itemSubtotal));

        // set delivery range text
//        if ("1".equals(deliveryRange)) {
//            deliveryRangeVw.setText(r.getQuantityString(R.plurals.business_days, 1, "1"));
//        } else {
//            deliveryRangeVw.setText(r.getQuantityString(R.plurals.business_days, 2, deliveryRange));
//        }

        // get api objects
        secureApi = Access.getInstance().getEasyOpenApi(true);

        // create api listeners
        shippingChargeListener = new CartListener(true, false);
        taxListener = new CartListener(false, true);
        precheckoutListener = new PrecheckoutListener();

        // initialize data prior to making api calls which will fill it
        tax = null;


        // allow sub-classes to do there initialization
        initEntryArea(view);

        return view;
    }


    @Override
    public void onResume() {
        super.onResume();

        // update action bar
        activity.showCheckoutActionBarEntities();
        activity.setActionBarTitle(getResources().getString(this instanceof GuestCheckoutFragment?
                R.string.guest_checkout_title : R.string.checkout_title));
    }

    /** override this to handle other clicks, but call this super method */
    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.co_submission_layout:
                onSubmit();
                break;
        }
    }

    /** override this to handle order submission */
    protected abstract void onSubmit();

    /** override this to specify layout for entry area */
    protected abstract int getEntryLayoutId();

    /** override this for variation on entry area */
    protected abstract void initEntryArea(View view);


    protected void startPrecheckout() {
        showProgressIndicator();
        secureApi.precheckout(RECOMMENDATION, STORE_ID, LOCALE, CLIENT_ID, precheckoutListener);
    }

    protected void submitOrder(String cid) {
        // upon payment method success, submit the order
        SubmitOrderRequest submitOrderRequest = new SubmitOrderRequest();
        submitOrderRequest.setCardVerificationCode(cid);
        secureApi.submitOrder(submitOrderRequest, RECOMMENDATION, STORE_ID, LOCALE, CLIENT_ID,
                new Callback<SubmitOrderResponse>() {

                    @Override
                    public void success(SubmitOrderResponse submitOrderResponse, Response response) {
                        hideProgressIndicator();

                        // show confirmation page and refresh cart
                        ((MainActivity)activity).selectOrderConfirmation(
                                submitOrderResponse.getOrderId(),
                                submitOrderResponse.getStaplesOrderNumber());
                    }

                    @Override
                    public void failure(RetrofitError retrofitError) {
                        hideProgressIndicator();
                        Toast.makeText(activity, "Submission Error: " + ApiError.getErrorMessage(retrofitError), Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }



    protected void showProgressIndicator() {
        checkoutLayout.getProgressIndicator().showProgressIndicator();
    }

    protected void hideProgressIndicator() {
        checkoutLayout.getProgressIndicator().hideProgressIndicator();
    }


    /** parses shipping charge if possible (might be "Free") and formats for currency */
    public static String formatShippingCharge(String shippingCharge, NumberFormat currencyFormat) {
        try { // if possible, parse floating value and format as money
            shippingCharge = currencyFormat.format(Float.parseFloat(shippingCharge));
        } catch(NumberFormatException e) { /* normal to fail (e.g. if equal to "Free") */}
        return shippingCharge;
    }

    // Retrofit callbacks


    /************* api listeners ************/


    /** listens for completion of view request */
    class CartListener implements Callback<CartContents> {

        boolean shippingChargeListener; // true if shipping charge listener
        boolean taxListener; // true if tax listener

        CartListener(boolean shippingChargeListener, boolean taxListener) {
            this.shippingChargeListener = shippingChargeListener;
            this.taxListener = taxListener && !shippingChargeListener;
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
                taxLayout.setVisibility(View.VISIBLE);
                if (pretaxSubtotal != null && tax != null) {
                    checkoutTotalVw.setText(currencyFormat.format(pretaxSubtotal + tax));
                    submissionLayout.setVisibility(View.VISIBLE);
                }
                hideProgressIndicator();
            }

            if (shippingChargeListener) {
                shippingChargeVw.setText(formatShippingCharge(cart.getShippingCharge(), currencyFormat));
                shippingChargeLayout.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void failure(RetrofitError retrofitError) {
            String msg = "Error getting math story: " + ApiError.getErrorMessage(retrofitError);
            Log.d(TAG, msg);
            Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show();

            if (taxListener) {
                hideProgressIndicator();
            }
        }
    }


    /** listens for completion of precheckout */
    class PrecheckoutListener implements Callback<AddressValidationAlert> {

        @Override
        public void success(AddressValidationAlert precheckoutResponse, Response response) {
            // get tax and shipping charge
            secureApi.getTax(RECOMMENDATION, STORE_ID, LOCALE, CLIENT_ID, taxListener);
            secureApi.getShippingCharge(RECOMMENDATION, STORE_ID, LOCALE, CLIENT_ID, shippingChargeListener);
        }

        @Override
        public void failure(RetrofitError retrofitError) {
            String msg = "Precheckout error: " + ApiError.getErrorMessage(retrofitError);
            Log.d(TAG, msg);
            Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show();
            hideProgressIndicator();
        }
    }

}
