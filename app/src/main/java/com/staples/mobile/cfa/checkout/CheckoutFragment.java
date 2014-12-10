/*
 * Copyright (c) 2014 Staples, Inc. All rights reserved.
 */

package com.staples.mobile.cfa.checkout;

import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.staples.mobile.cfa.BaseFragment;
import com.staples.mobile.cfa.MainActivity;
import com.staples.mobile.cfa.R;
import com.staples.mobile.cfa.login.LoginHelper;
import com.staples.mobile.common.access.Access;
import com.staples.mobile.common.access.easyopen.api.EasyOpenApi;
import com.staples.mobile.common.access.easyopen.model.ApiError;
import com.staples.mobile.common.access.easyopen.model.cart.Cart;
import com.staples.mobile.common.access.easyopen.model.cart.CartContents;
import com.staples.mobile.common.access.easyopen.model.checkout.AddressValidationAlert;
import com.staples.mobile.common.access.easyopen.model.checkout.SubmitOrderRequest;
import com.staples.mobile.common.access.easyopen.model.checkout.SubmitOrderResponse;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


public abstract class CheckoutFragment extends BaseFragment implements View.OnClickListener {
    private static final String TAG = CheckoutFragment.class.getSimpleName();

    private static final String RECOMMENDATION = "v1";
    private static final String STORE_ID = "10001";

    private static final String CATALOG_ID = "10051";
    private static final String LOCALE = "en_US";

    private static final String CLIENT_ID = LoginHelper.CLIENT_ID;

    private static final int MAXFETCH = 50;

    // bundle param keys
    public static final String BUNDLE_PARAM_COUPONSREWARDS = "couponsRewards";
    public static final String BUNDLE_PARAM_ITEMSUBTOTAL = "itemSubtotal";
    public static final String BUNDLE_PARAM_PRETAXSUBTOTAL = "preTaxSubtotal";
    public static final String BUNDLE_PARAM_SHIPPING_CHARGE = "shippingCharge";
    public static final String BUNDLE_PARAM_TAX = "tax";

    private DecimalFormat currencyFormat;

    // saving around Activity object since getActivity() returns null after user navigates away from
    // fragment, but api call may still be returning
    protected MainActivity activity;

    private View shippingChargeLayout;
    private View taxLayout;
    private View submissionLayout;
    private ViewGroup checkoutEntryLayout;
    private TextView itemSubtotalVw;
    private TextView couponsRewardsVw;
    private TextView shippingChargeVw;
    private TextView taxVw;
    private TextView checkoutTotalVw;


    // api objects
    protected EasyOpenApi secureApi;

    // data returned from api
    private Float tax;
    private String shippingCharge;


    // data initialized from cart drawer
    private Float couponsRewardsAmount;
    private Float itemSubtotal;
    private Float pretaxSubtotal;


    // api listeners
    PrecheckoutListener precheckoutListener;

    protected CheckoutFragment() {
        // set up currency format to use minus sign for negative amounts (needed for coupons)
        currencyFormat = (DecimalFormat)NumberFormat.getCurrencyInstance();
        String symbol = currencyFormat.getCurrency().getSymbol();
        currencyFormat.setNegativePrefix("-"+symbol);
        currencyFormat.setNegativeSuffix("");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        Log.d(TAG, "onCreateView()");

        Resources r = getResources();

        activity = (MainActivity)getActivity();

        // inflate and get child views
        View view = inflater.inflate(R.layout.checkout_fragment, container, false);
        checkoutEntryLayout = (ViewGroup)view.findViewById(R.id.checkout_entry_layout);
        inflater.inflate(getEntryLayoutId(), checkoutEntryLayout); // dynamically inflate variable entry area
        shippingChargeLayout = view.findViewById(R.id.co_shipping_layout);
        submissionLayout = view.findViewById(R.id.co_submission_layout);
        taxLayout = view.findViewById(R.id.co_tax_layout);
        itemSubtotalVw = (TextView) view.findViewById(R.id.checkout_item_subtotal);
        couponsRewardsVw = (TextView) view.findViewById(R.id.checkout_coupons_rewards);
        shippingChargeVw = (TextView) view.findViewById(R.id.checkout_shipping);
        taxVw = (TextView) view.findViewById(R.id.checkout_tax);
        checkoutTotalVw = (TextView) view.findViewById(R.id.checkout_order_total);

        // Set click listeners
        submissionLayout.setOnClickListener(this);

        // get checkout info from bundle
        Bundle checkoutBundle = this.getArguments();
        couponsRewardsAmount = checkoutBundle.getFloat(BUNDLE_PARAM_COUPONSREWARDS);
        itemSubtotal = checkoutBundle.getFloat(BUNDLE_PARAM_ITEMSUBTOTAL);
        pretaxSubtotal = checkoutBundle.getFloat(BUNDLE_PARAM_PRETAXSUBTOTAL);
        shippingCharge = checkoutBundle.getString(BUNDLE_PARAM_SHIPPING_CHARGE);
        tax = checkoutBundle.getFloat(BUNDLE_PARAM_TAX, -1);
        if (tax == -1) {
            tax = null;
        }

        // set coupons/rewards adjusted amount
        couponsRewardsVw.setText(currencyFormat.format(couponsRewardsAmount));

        // set the item subtotal
        itemSubtotalVw.setText(currencyFormat.format(itemSubtotal));

        // get api objects
        secureApi = Access.getInstance().getEasyOpenApi(true);

        // create api listeners
        precheckoutListener = new PrecheckoutListener();

        // allow sub-classes to do there initialization
        initEntryArea(view);

        return view;
    }


    @Override
    public void onResume() {
        super.onResume();

        // update action bar
        activity.showCheckoutActionBarEntities();
        int titleId = this instanceof GuestCheckoutFragment ? R.string.guest_checkout_title : R.string.checkout_title;
        activity.showActionBar(titleId, 0, null);
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

    /** handles order submission */
    protected abstract void onSubmit();

    /** specifies layout for variable entry area */
    protected abstract int getEntryLayoutId();

    /** initializes variable entry area of checkout screen */
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
        activity.showProgressIndicator();
    }

    protected void hideProgressIndicator() {
        activity.hideProgressIndicator();
    }


    /** updates the shipping charge and tax values (may be result of api response or a call from the subclass) */
    protected void setShippingAndTax(String shippingCharge, float tax){
        Bundle checkoutBundle = this.getArguments();
        checkoutBundle.putString(BUNDLE_PARAM_SHIPPING_CHARGE, shippingCharge);
        checkoutBundle.putFloat(BUNDLE_PARAM_TAX, tax);
        this.shippingCharge = shippingCharge;
        this.tax = tax;
        shippingChargeVw.setText(formatShippingCharge(shippingCharge, currencyFormat));
        taxVw.setText(currencyFormat.format(tax));
        checkoutTotalVw.setText(currencyFormat.format(pretaxSubtotal + tax)); // coupons/rewards are already factored into pretaxSubtotal
        taxLayout.setVisibility(View.VISIBLE);
        shippingChargeLayout.setVisibility(View.VISIBLE);
        submissionLayout.setVisibility(View.VISIBLE);
    }

    /** returns tax value if available */
    protected Float getTax() {
        return this.tax;
    }

    /** returns tax value if available */
    protected String getShippingCharge() {
        return this.shippingCharge;
    }

    public Float getItemSubtotal() {
        return this.itemSubtotal;
    }

    public Float getPretaxSubtotal() {
        return this.pretaxSubtotal;
    }

    /** parses shipping charge if possible (might be "Free") and formats for currency */
    public static String formatShippingCharge(String shippingCharge, NumberFormat currencyFormat) {
        if (shippingCharge == null) { // working around a temporary bug where the cart returns null
            shippingCharge = "";
        }
        try { // if possible, parse floating value and format as money
            shippingCharge = currencyFormat.format(Float.parseFloat(shippingCharge));
        } catch(NumberFormatException e) { /* normal to fail (e.g. if equal to "Free") */}
        return shippingCharge;
    }



    /************* api listeners ************/


    /** listens for completion of precheckout */
    class PrecheckoutListener implements Callback<AddressValidationAlert> {

        String shippingCharge;

        @Override
        public void success(AddressValidationAlert precheckoutResponse, Response response) {
            // get shipping charge, then get tax
            secureApi.getShippingCharge(RECOMMENDATION, STORE_ID, LOCALE, CLIENT_ID, new Callback<CartContents>() {
                @Override
                public void success(CartContents cartContents, Response response) {

                    Cart cart = getCartFromResponse(cartContents);
                    if (cart != null) {
                        shippingCharge = cart.getShippingCharge();
                        secureApi.getTax(RECOMMENDATION, STORE_ID, LOCALE, CLIENT_ID, new Callback<CartContents>() {
                            @Override
                            public void success(CartContents cartContents, Response response) {
                                Cart cart = getCartFromResponse(cartContents);
                                hideProgressIndicator();
                                setShippingAndTax(shippingCharge, cart.getTotalTax());
                            }

                            @Override
                            public void failure(RetrofitError retrofitError) {
                                handleFailure("Error retrieving tax: " + ApiError.getErrorMessage(retrofitError));
                            }
                        });

                    } else {
                        handleFailure("Error retrieving shipping charge");
                    }
                }

                @Override
                public void failure(RetrofitError retrofitError) {
                    handleFailure("Error retrieving shipping charge: " + ApiError.getErrorMessage(retrofitError));
                }

                private Cart getCartFromResponse(CartContents cartContents) {
                    if (cartContents != null && cartContents.getCart() != null && cartContents.getCart().size() > 0) {
                        return cartContents.getCart().get(0);
                    }
                    return null;
                }
            });
        }

        @Override
        public void failure(RetrofitError retrofitError) {
            handleFailure("Precheckout error: " + ApiError.getErrorMessage(retrofitError));
        }

        private void handleFailure(String msg) {
            Log.d(TAG, msg);
            Toast.makeText(activity, msg, Toast.LENGTH_LONG).show();
            hideProgressIndicator();
        }
    }

}
