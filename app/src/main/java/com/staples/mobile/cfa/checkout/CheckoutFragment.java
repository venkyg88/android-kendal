/*
 * Copyright (c) 2014 Staples, Inc. All rights reserved.
 */

package com.staples.mobile.cfa.checkout;

import android.app.Fragment;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.staples.mobile.cfa.MainActivity;
import com.staples.mobile.cfa.R;
import com.staples.mobile.common.access.easyopen.model.cart.PaymentMethod;
import com.staples.mobile.common.access.easyopen.model.cart.ShippingAddress;
import com.staples.mobile.common.analytics.Tracker;
import com.staples.mobile.cfa.cart.CartApiManager;
import com.staples.mobile.cfa.util.CurrencyFormat;
import com.staples.mobile.cfa.widget.ActionBar;

import java.text.DecimalFormat;
import java.text.NumberFormat;


public abstract class CheckoutFragment extends Fragment implements View.OnClickListener {
    public static final String TAG = CheckoutFragment.class.getSimpleName();

    // bundle param keys
    public static final String BUNDLE_PARAM_COUPONSREWARDS = "couponsRewards";
    public static final String BUNDLE_PARAM_ITEMSUBTOTAL = "itemSubtotal";
    public static final String BUNDLE_PARAM_PRETAXSUBTOTAL = "preTaxSubtotal";
    public static final String BUNDLE_PARAM_DELIVERY_RANGE = "deliveryRange";
    public static final String BUNDLE_PARAM_SHIPPING_CHARGE = "shippingCharge";
    public static final String BUNDLE_PARAM_TAX = "tax";

    // saving around Activity object since getActivity() returns null after user navigates away from
    // fragment, but api call may still be returning
    protected MainActivity activity;

    private View submissionLayout;
    private ViewGroup checkoutEntryLayout;
    private TextView itemSubtotalVw;
    private TextView couponsRewardsVw;
    private TextView shippingChargeVw;
    private TextView shippingChargeLabelVw;
    private TextView taxLabelVw;
    private TextView taxVw;
    private TextView checkoutTotalVw;

    private int greenText;
    private int blackText;

    // data returned from api
    private Float tax;
    private String shippingCharge;


    // data initialized from cart drawer
    private Float couponsRewardsAmount;
    private Float itemSubtotal;
    private Float pretaxSubtotal;
    private String deliveryRange;


    /**
     * Create a new instance of RegisteredCheckoutFragment that will be initialized
     * with the given arguments. Used when opening a fresh checkout session from the cart.
     */
    public static Bundle createInitialBundle(float couponsRewardsAmount, float itemSubtotal, float preTaxSubtotal, String deliveryRange) {
        Bundle args = new Bundle();
        args.putFloat(CheckoutFragment.BUNDLE_PARAM_COUPONSREWARDS, couponsRewardsAmount);
        args.putFloat(CheckoutFragment.BUNDLE_PARAM_ITEMSUBTOTAL, itemSubtotal);
        args.putFloat(CheckoutFragment.BUNDLE_PARAM_PRETAXSUBTOTAL, preTaxSubtotal);
        args.putString(CheckoutFragment.BUNDLE_PARAM_DELIVERY_RANGE, deliveryRange);
        return args;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        Resources r = getResources();

        activity = (MainActivity)getActivity();

        // inflate and get child views
        View view = inflater.inflate(R.layout.checkout_fragment, container, false);
        checkoutEntryLayout = (ViewGroup)view.findViewById(R.id.checkout_entry_layout);
        inflater.inflate(getEntryLayoutId(), checkoutEntryLayout); // dynamically inflate variable entry area
//        shippingChargeLayout = view.findViewById(R.id.co_shipping_layout);
        submissionLayout = view.findViewById(R.id.co_submission_layout);
//        taxLayout = view.findViewById(R.id.co_tax_layout);
        itemSubtotalVw = (TextView) view.findViewById(R.id.checkout_item_subtotal);
        couponsRewardsVw = (TextView) view.findViewById(R.id.checkout_coupons_rewards);
        shippingChargeVw = (TextView) view.findViewById(R.id.checkout_shipping);
        shippingChargeLabelVw = (TextView) view.findViewById(R.id.checkout_shipping_label);
        taxVw = (TextView) view.findViewById(R.id.checkout_tax);
        taxLabelVw = (TextView) view.findViewById(R.id.checkout_tax_label);
        checkoutTotalVw = (TextView) view.findViewById(R.id.checkout_order_total);

        greenText = r.getColor(R.color.text_green);
        blackText = r.getColor(R.color.text_nearly_black);

        // Set click listeners
        submissionLayout.setOnClickListener(this);

        // get checkout info from bundle
        Bundle checkoutBundle = this.getArguments();
        couponsRewardsAmount = checkoutBundle.getFloat(BUNDLE_PARAM_COUPONSREWARDS);
        itemSubtotal = checkoutBundle.getFloat(BUNDLE_PARAM_ITEMSUBTOTAL);
        pretaxSubtotal = checkoutBundle.getFloat(BUNDLE_PARAM_PRETAXSUBTOTAL);
        shippingCharge = checkoutBundle.getString(BUNDLE_PARAM_SHIPPING_CHARGE);
        deliveryRange = checkoutBundle.getString(BUNDLE_PARAM_DELIVERY_RANGE);
        tax = checkoutBundle.getFloat(BUNDLE_PARAM_TAX, -1);
        if (tax == -1) {
            tax = null;
        }

        DecimalFormat currencyFormat = CurrencyFormat.getFormatter();

        // set coupons/rewards adjusted amount
        couponsRewardsVw.setText(currencyFormat.format(couponsRewardsAmount));

        // set the item subtotal
        itemSubtotalVw.setText(currencyFormat.format(itemSubtotal));

        // allow sub-classes to do their initialization
        initEntryArea(view);

        return view;
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
    protected abstract void initEntryArea(View frame);


    protected void startPrecheckout() {
        showProgressIndicator();
        CheckoutApiManager.precheckout(new CheckoutApiManager.PrecheckoutCallback() {
            @Override
            public void onPrecheckoutComplete(String shippingCharge, Float tax, String errMsg, String infoMsg) {
                hideProgressIndicator();

                // if success
                if (errMsg == null) {

                    // if address alert, display it
                    if (infoMsg != null) {
                        activity.showErrorDialog(infoMsg);
                    }

                    // utilize shipping and tax info
                    setShippingAndTax(shippingCharge, tax);

                } else {
                    // if shipping and tax already showing, need to hide them
                    resetShippingAndTax();

                    activity.showErrorDialog(errMsg);
                    Log.d(TAG, errMsg);
                }
            }
        });
    }


    protected void submitOrder(final PaymentMethod paymentMethod, final String emailAddress) {
        showProgressIndicator();
        CheckoutApiManager.submitOrder(paymentMethod.getCardVerificationCode(), new CheckoutApiManager.OrderSubmissionCallback() {
            @Override
            public void onOrderSubmissionComplete(String orderId, String orderNumber, String errMsg) {
                hideProgressIndicator();

                final DecimalFormat currencyFormat = CurrencyFormat.getFormatter();

                // if success
                if (errMsg == null) {

                    // analytics
                    Tracker.getInstance().trackStateForOrderConfirmation(orderNumber,
                            CartApiManager.getCart(), Math.abs(couponsRewardsAmount), paymentMethod,
                            Tracker.ShipType.SHIPTOHOME);

                    // reset cart since empty after successful order submission
                    CartApiManager.resetCart(); // reset cart since empty after successful order submission
                    ActionBar.getInstance().setCartCount(0);

                    // show confirmation page
                    activity.selectOrderConfirmation(orderNumber, emailAddress,
                            deliveryRange, currencyFormat.format(getCheckoutTotal()));

                } else {
                    Tracker.getInstance().trackActionForCheckoutFormErrors(errMsg);
                    activity.showErrorDialog(errMsg);
                    Log.d(TAG, errMsg);

                    // sometimes there's a failure such as timeout but the order actually goes thru.
                    // therefore, refresh the cart to make sure we have the right cart status.
                    // (note that even this safeguard sometimes fails because order submission is still
                    // in process following the timeout and this reloading of the cart is too
                    // soon (items still returned)
                    CartApiManager.loadCart(new CartApiManager.CartRefreshCallback() {
                        @Override public void onCartRefreshComplete(String errMsg) {
                            if (CartApiManager.getCartTotalItems() == 0) {
                                activity.showErrorDialog(R.string.order_confirmation_with_error);
                                ActionBar.getInstance().setCartCount(0);
                                // show confirmation page
                                activity.selectOrderConfirmation("(see email)", emailAddress,
                                        deliveryRange, currencyFormat.format(getCheckoutTotal()));
                            }
                        }
                    });

                }
            }
        });
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
        DecimalFormat currencyFormat = CurrencyFormat.getFormatter();
        shippingChargeVw.setText(formatShippingCharge(shippingCharge, currencyFormat));
        shippingChargeVw.setTextColor("Free".equals(shippingCharge) ? greenText : blackText);
        taxVw.setText(currencyFormat.format(tax));
        checkoutTotalVw.setText(currencyFormat.format(getCheckoutTotal()));
        setShipTaxSubmitVisibility(true);
    }

    /** updates the shipping charge and tax values (may be result of api response or a call from the subclass) */
    protected void resetShippingAndTax() {
        if (submissionLayout.getVisibility() == View.VISIBLE) {
            Bundle checkoutBundle = this.getArguments();
            checkoutBundle.putString(BUNDLE_PARAM_SHIPPING_CHARGE, null);
            checkoutBundle.putFloat(BUNDLE_PARAM_TAX, -1);
            this.shippingCharge = null;
            this.tax = null;
            setShipTaxSubmitVisibility(false);
        }
    }

    private void setShipTaxSubmitVisibility(boolean visible) {
        shippingChargeVw.setVisibility(visible? View.VISIBLE : View.GONE);
        shippingChargeLabelVw.setVisibility(visible? View.VISIBLE : View.GONE);
        taxVw.setVisibility(visible? View.VISIBLE : View.GONE);
        taxLabelVw.setVisibility(visible? View.VISIBLE : View.GONE);
        submissionLayout.setVisibility(visible? View.VISIBLE : View.GONE);
    }

    private Float getCheckoutTotal() {
        return pretaxSubtotal + tax; // coupons/rewards are already factored into pretaxSubtotal
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
}
