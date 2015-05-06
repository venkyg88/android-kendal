package app.staples.mobile.cfa.checkout;

import android.app.Fragment;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.crittercism.app.Crittercism;
import com.staples.mobile.common.access.easyopen.model.cart.Cart;
import com.staples.mobile.common.access.easyopen.model.cart.PaymentMethod;
import com.staples.mobile.common.analytics.Tracker;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import app.staples.R;
import app.staples.mobile.cfa.MainActivity;
import app.staples.mobile.cfa.cart.CartApiManager;
import app.staples.mobile.cfa.kount.KountManager;
import app.staples.mobile.cfa.util.CurrencyFormat;
import app.staples.mobile.cfa.widget.ActionBar;

public abstract class CheckoutFragment extends Fragment implements View.OnClickListener {

   protected enum ButtonState {
       ENABLED, DISABLED
   }

    public static final String TAG = CheckoutFragment.class.getSimpleName();

    // bundle param keys
    public static final String BUNDLE_PARAM_TOTAL_HANDLING_COST = "totalHandlingCost";
    public static final String BUNDLE_PARAM_SHIPPING_CHARGE = "shippingCharge";
    public static final String BUNDLE_PARAM_TAX = "tax";

    // saving around Activity object since getActivity() returns null after user navigates away from
    // fragment, but api call may still be returning
    protected MainActivity activity;

    private CardView submissionLayout;
    private ViewGroup checkoutEntryLayout;
    private TextView itemSubtotalVw;
//    private TextView couponsRewardsVw;
    private TextView oversizedShippingVw;
    private TextView oversizedShippingLabelVw;
    private TextView shippingChargeVw;
    private TextView shippingChargeLabelVw;
    private TextView taxLabelVw;
    private TextView taxVw;
    private TextView checkoutTotalVw;

    private int greenText;
    private int blackText;

    // data returned from api
    private Float tax;
    private Float totalHandlingCost;
    private String shippingCharge;

    // data initialized from cart drawer
//    private Float couponsRewardsAmount;
    private Float itemSubtotal;
    private Float pretaxSubtotal;
    private String deliveryRange;

    private ButtonState buttonState;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        Crittercism.leaveBreadcrumb("CheckoutFragment:onCreateView(): Displaying the Checkout screen.");
        Resources r = getResources();

        activity = (MainActivity)getActivity();

        // inflate and get child views
        View view = inflater.inflate(R.layout.checkout_fragment, container, false);
        checkoutEntryLayout = (ViewGroup)view.findViewById(R.id.checkout_entry_layout);
        inflater.inflate(getEntryLayoutId(), checkoutEntryLayout); // dynamically inflate variable entry area
//        shippingChargeLayout = view.findViewById(R.id.co_shipping_layout);
        submissionLayout = (CardView)view.findViewById(R.id.co_submission_layout);

//        taxLayout = view.findViewById(R.id.co_tax_layout);
        itemSubtotalVw = (TextView) view.findViewById(R.id.checkout_item_subtotal);
//        couponsRewardsVw = (TextView) view.findViewById(R.id.checkout_coupons_rewards);

        oversizedShippingVw = (TextView) view.findViewById(R.id.oversized_shipping);
        oversizedShippingLabelVw = (TextView) view.findViewById(R.id.oversized_shipping_label);

        shippingChargeVw = (TextView) view.findViewById(R.id.checkout_shipping);
        shippingChargeLabelVw = (TextView) view.findViewById(R.id.checkout_shipping_label);

        taxVw = (TextView) view.findViewById(R.id.checkout_tax);
        taxLabelVw = (TextView) view.findViewById(R.id.checkout_tax_label);
        checkoutTotalVw = (TextView) view.findViewById(R.id.checkout_order_total);

        greenText = r.getColor(R.color.staples_green);
        blackText = r.getColor(R.color.staples_black);

        // Set click listeners
        submissionLayout.setOnClickListener(this);

//        couponsRewardsAmount = CartApiManager.getCouponsRewardsAdjustedAmount();
        itemSubtotal = CartApiManager.getSubTotal();
        pretaxSubtotal = CartApiManager.getPreTaxTotal();
        deliveryRange = CartApiManager.getExpectedDeliveryRange();

        // get checkout info from bundle
        Bundle checkoutBundle = this.getArguments();
        totalHandlingCost = checkoutBundle.getFloat(BUNDLE_PARAM_TOTAL_HANDLING_COST);
        shippingCharge = checkoutBundle.getString(BUNDLE_PARAM_SHIPPING_CHARGE);
        if (deliveryRange != null) {
            deliveryRange += " " + getResources().getQuantityText(R.plurals.business_days,
                    deliveryRange.equals("1")? 1 : 2);
        }
        tax = checkoutBundle.getFloat(BUNDLE_PARAM_TAX, -1);
        if (tax == -1) {
            tax = null;
        }

        DecimalFormat currencyFormat = CurrencyFormat.getFormatter();

        // DLS: taking this out as per Tim (commenting-out for now just in case they ask for it back)
        // set coupons/rewards adjusted amount
//        couponsRewardsVw.setText(currencyFormat.format(couponsRewardsAmount));

        // set the item subtotal
        itemSubtotalVw.setText(currencyFormat.format(itemSubtotal));

        // allow sub-classes to do their initialization
        initEntryArea(view);

        // initiate Kount collection
        Cart cart = CartApiManager.getCart();
        if (cart!=null) {
            String orderId = cart.getOrderId();
            if (orderId!=null) {
                KountManager.getInstance().collect(orderId);
            }
        }

        return view;
    }

    /** override this to handle other clicks, but call this super method */
    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.co_submission_layout:
                if(buttonState == ButtonState.ENABLED) {
                    onSubmit();
                }
                break;
        }
    }

    /** handles order submission */
    protected abstract void onSubmit();

    /** specifies layout for variable entry area */
    protected abstract int getEntryLayoutId();

    /** initializes variable entry area of checkout screen */
    protected abstract void initEntryArea(View frame);

    protected void disableCheckoutButton(boolean isDisabled) {
        if(isDisabled) {
            submissionLayout.setCardBackgroundColor(getResources().getColor(R.color.staples_dark_gray));
            buttonState = ButtonState.DISABLED;
        } else {
            submissionLayout.setCardBackgroundColor(getResources().getColor(R.color.staples_red));
            buttonState = ButtonState.ENABLED;
        }
    }

    protected void startPrecheckout() {
        activity.showProgressIndicator();
        CheckoutApiManager.precheckout(new CheckoutApiManager.PrecheckoutCallback() {
            @Override
            public void onPrecheckoutComplete(Float totalHandlingCost, String shippingCharge, Float tax, String errMsg, String infoMsg) {
                if (getActivity() == null) { return; } // make sure fragment is still attached
                activity.hideProgressIndicator();
                // if success
                if (errMsg == null) {

                    // if address alert, display it
                    if (infoMsg != null) {
                        activity.showErrorDialog(infoMsg);
                    }
                    disableCheckoutButton(false);
                    // utilize shipping and tax info
                    setShippingAndTax(totalHandlingCost, shippingCharge, tax);

                    hideProgressIndicator();
                } else {
                    // if shipping and tax already showing, need to hide them
                    resetShippingAndTax();
                    hideProgressIndicator();
                    if (errMsg.contains("Please sign in")) { // full message: "Guest Checkout Not Available: Because of one or more items in your Cart, Guest Checkout is not available. Please sign in or create an account to continue.""
                        errMsg = getResources().getString(R.string.premium_product_requires_login);
                    }
                    activity.showErrorDialog(errMsg);
                    Log.d(TAG, errMsg);
                }
            }
        });
    }

    protected void submitOrder(final PaymentMethod paymentMethod, final String emailAddress) {
        CheckoutApiManager.submitOrder(paymentMethod.getCardVerificationCode(), new CheckoutApiManager.OrderSubmissionCallback() {
            @Override
            public void onOrderSubmissionComplete(String orderId, String orderNumber, String errMsg) {

                final DecimalFormat currencyFormat = CurrencyFormat.getFormatter();

                // if success
                if (errMsg == null) {

                    // analytics (do this before resetting cart below!)
                    Tracker.getInstance().trackStateForOrderConfirmation(orderNumber,
                            CartApiManager.getCart(), paymentMethod, Tracker.ShipType.SHIPTOHOME);

                    // reset cart since empty after successful order submission
                    CartApiManager.resetCart(); // reset cart since empty after successful order submission
                    ActionBar.getInstance().setCartCount(0);
                    hideProgressIndicator();

                    // show confirmation page
                    activity.selectOrderConfirmation(orderNumber, emailAddress,
                            deliveryRange, currencyFormat.format(getCheckoutTotal()));

                } else {
                    Tracker.getInstance().trackActionForCheckoutFormErrors(errMsg);
                    activity.showErrorDialog(errMsg);
                    Log.d(TAG, errMsg);
                    hideProgressIndicator();
                    // sometimes there's a failure such as timeout but the order actually goes thru.
                    // therefore, refresh the cart to make sure we have the right cart status.
                    // (note that even this safeguard sometimes fails because order submission is still
                    // in process following the timeout and this reloading of the cart is too
                    // soon (items still returned)
                    CartApiManager.loadCart(new CartApiManager.CartRefreshCallback() {
                        @Override
                        public void onCartRefreshComplete(String errMsg) {
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
    protected void setShippingAndTax(float totalHandlingCost, String shippingCharge, float tax){

        Bundle checkoutBundle = this.getArguments();
        checkoutBundle.putFloat(BUNDLE_PARAM_TOTAL_HANDLING_COST, totalHandlingCost);
        checkoutBundle.putString(BUNDLE_PARAM_SHIPPING_CHARGE, shippingCharge);
        checkoutBundle.putFloat(BUNDLE_PARAM_TAX, tax);

        this.totalHandlingCost = totalHandlingCost;
        this.shippingCharge = shippingCharge;
        this.tax = tax;

        DecimalFormat currencyFormat = CurrencyFormat.getFormatter();
        String totalHandlingCostStr = currencyFormat.format(totalHandlingCost);
        oversizedShippingVw.setText(totalHandlingCostStr);

        shippingChargeVw.setText(formatShippingCharge(shippingCharge, currencyFormat));
        shippingChargeVw.setTextColor("Free".equals(shippingCharge) ? greenText : blackText);

        String taxStr = currencyFormat.format(tax);
        taxVw.setText(taxStr);

        checkoutTotalVw.setText(currencyFormat.format(getCheckoutTotal()));
    }

    /** updates the shipping charge and tax values (may be result of api response or a call from the subclass) */
    protected void resetShippingAndTax() {
        if (submissionLayout.getVisibility() == View.VISIBLE) {
            Bundle checkoutBundle = this.getArguments();
            checkoutBundle.putFloat(BUNDLE_PARAM_TOTAL_HANDLING_COST, -1);
            checkoutBundle.putString(BUNDLE_PARAM_SHIPPING_CHARGE, null);
            checkoutBundle.putFloat(BUNDLE_PARAM_TAX, -1);
            this.shippingCharge = null;
            this.tax = null;
        }
    }

    private Float getCheckoutTotal() {
        float total = pretaxSubtotal.floatValue();
        if (tax != null) {
            total += tax.floatValue();
        }
        return total; // coupons/rewards are already factored into pretaxSubtotal
    }

    /** returns tax value if available */
    protected Float getTax() {
        return this.tax;
    }

    /** returns tax value if available */
    protected Float getTotalHandlingCost() {
        return this.totalHandlingCost;
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
