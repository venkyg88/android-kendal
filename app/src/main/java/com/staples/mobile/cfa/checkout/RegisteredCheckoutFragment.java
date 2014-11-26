/*
 * Copyright (c) 2014 Staples, Inc. All rights reserved.
 */

package com.staples.mobile.cfa.checkout;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.staples.mobile.cfa.R;
import com.staples.mobile.cfa.MainActivity;
import com.staples.mobile.cfa.login.LoginHelper;
import com.staples.mobile.cfa.profile.ProfileDetails;
import com.staples.mobile.common.access.Access;
import com.staples.mobile.common.access.easyopen.model.ApiError;
import com.staples.mobile.common.access.easyopen.model.cart.BillingAddress;
import com.staples.mobile.common.access.easyopen.model.cart.PaymentMethod;
import com.staples.mobile.common.access.easyopen.model.cart.PaymentMethodResponse;
import com.staples.mobile.common.access.easyopen.model.cart.ShippingAddress;
import com.staples.mobile.common.access.easyopen.model.checkout.AddressValidationAlert;
import com.staples.mobile.common.access.easyopen.model.member.CCDetails;
import com.staples.mobile.common.access.easyopen.model.member.Member;

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

    // additional bundle param keys
    public static final String BUNDLE_PARAM_SHIPPING_ADDR_ID = "shippingAddrId";
    public static final String BUNDLE_PARAM_BILLING_ADDR_ID = "billingAddrId";
    public static final String BUNDLE_PARAM_PAYMENT_METHOD_ID = "paymentMethodId";


    private TextView shippingAddrVw;
    private TextView paymentMethodVw;
    private TextView billingAddrVw;

//    ProfileSelections profileSelections;
    // profile selections
    String shippingAddressId;
    String paymentMethodId;
    String billingAddressId;

    /**
     * Create a new instance of RegisteredCheckoutFragment that will be initialized
     * with the given arguments. Used when opening a fresh checkout session from the cart.
     */
    public static CheckoutFragment newInstance(float itemSubtotal, float preTaxSubtotal) {
        CheckoutFragment f = new RegisteredCheckoutFragment();
        Bundle args = new Bundle();
        args.putFloat(CheckoutFragment.BUNDLE_PARAM_ITEMSUBTOTAL, itemSubtotal);
        args.putFloat(CheckoutFragment.BUNDLE_PARAM_PRETAXSUBTOTAL, preTaxSubtotal);
        f.setArguments(args);
        return f;
    }

    /** specifies layout for variable entry area */
    @Override
    protected int getEntryLayoutId() {
        return R.layout.checkout_registered_entry;
    }

    /** initializes variable entry area of checkout screen */
    @Override
    protected void initEntryArea(View view) {

        // init views
        shippingAddrVw = (TextView) view.findViewById(R.id.checkout_shipping_addr);
        paymentMethodVw = (TextView) view.findViewById(R.id.checkout_payment_method);
        billingAddrVw = (TextView) view.findViewById(R.id.checkout_billing_addr);

        // Set click listeners
        view.findViewById(R.id.shipping_addresses).setOnClickListener(this);
        view.findViewById(R.id.payment_methods).setOnClickListener(this);
        view.findViewById(R.id.billing_addresses).setOnClickListener(this);

        // get additional checkout info from bundle (tax, shipping, and subtotals retrieved by super class)
        Bundle checkoutBundle = this.getArguments();
        shippingAddressId = checkoutBundle.getString(BUNDLE_PARAM_SHIPPING_ADDR_ID);
        paymentMethodId = checkoutBundle.getString(BUNDLE_PARAM_PAYMENT_METHOD_ID);
        billingAddressId = checkoutBundle.getString(BUNDLE_PARAM_BILLING_ADDR_ID);

        // initialize null items with profile data
        Member member = ProfileDetails.getMember();
        if (member != null) {
            List<com.staples.mobile.common.access.easyopen.model.member.Address> profileAddresses = ProfileDetails.getMember().getAddress();
            if (profileAddresses != null && profileAddresses.size() > 0) {
                String addrId = profileAddresses.get(0).getAddressId();
                if (shippingAddressId == null) {
                    shippingAddressId = addrId;
                    checkoutBundle.putString(BUNDLE_PARAM_SHIPPING_ADDR_ID, addrId);
                }
                if (billingAddressId == null) {
                    billingAddressId = addrId;
                    checkoutBundle.putString(BUNDLE_PARAM_BILLING_ADDR_ID, addrId);
                }
            }
            if (paymentMethodId == null) {
                List<CCDetails> profileCreditCards = ProfileDetails.getMember().getCreditCard();
                if (profileCreditCards != null && profileCreditCards.size() > 0) {
                    String ccId = profileCreditCards.get(0).getCreditCardId();
                    paymentMethodId = ccId;
                    checkoutBundle.putString(BUNDLE_PARAM_PAYMENT_METHOD_ID, ccId);
                }
            }
        }

        // set widget text with checkout selections
        com.staples.mobile.common.access.easyopen.model.member.Address shippingAddress = ProfileDetails.getAddress(shippingAddressId);
        com.staples.mobile.common.access.easyopen.model.member.Address billingAddress = ProfileDetails.getAddress(billingAddressId);
        CCDetails paymentMethod = ProfileDetails.getPaymentMethod(paymentMethodId);
        if (shippingAddress != null) {
            shippingAddrVw.setText(formatAddress(shippingAddress));
        }
        if (paymentMethod != null) {
            paymentMethodVw.setText(formatPaymentMethod(paymentMethod));
        }
        if (billingAddress != null) {
            billingAddrVw.setText(formatAddress(billingAddress));
        }

        // get api object
        secureApi = Access.getInstance().getEasyOpenApi(true);

        // initiate precheckout if necessary, otherwise update screen with shipping and tax
        if (shippingAddress != null) {
            if (getTax() == null || getShippingCharge() == null) {
                applyShippingAddressAndPrecheckout();
            } else {
                setShippingAndTax(getShippingCharge(), getTax());
            }
        }
    }



    /** applies shipping address to cart and initiates precheckout */
    private void applyShippingAddressAndPrecheckout() {
        final com.staples.mobile.common.access.easyopen.model.member.Address shippingAddress = ProfileDetails.getAddress(shippingAddressId);
        if (shippingAddress != null) {
            showProgressIndicator();
            secureApi.addShippingAddressToCart(new ShippingAddress(shippingAddress),
                    RECOMMENDATION, STORE_ID, LOCALE, CLIENT_ID, new Callback<AddressValidationAlert>() {
                        @Override
                        public void success(AddressValidationAlert addressValidationAlert, Response response) {
                            // applying the shipping address actually modifies the id on the server, so need to fix everything up
                            Bundle checkoutBundle = RegisteredCheckoutFragment.this.getArguments();
                            String oldId = shippingAddressId;
                            String newId = addressValidationAlert.getShippingAddressId();
                            shippingAddress.setAddressId(newId); // fix id in the profile
                            shippingAddressId = newId; // fix our local id
                            checkoutBundle.putString(BUNDLE_PARAM_SHIPPING_ADDR_ID, newId); // fix id in bundle
                            if (oldId.equals(billingAddressId)) {
                                billingAddressId = newId; // fix our local id
                                checkoutBundle.putString(BUNDLE_PARAM_BILLING_ADDR_ID, newId); // fix id in bundle
                            }

                            startPrecheckout();
                        }

                        @Override
                        public void failure(RetrofitError retrofitError) {
                            hideProgressIndicator();
                            String msg = "Address error: " + ApiError.getErrorMessage(retrofitError);
                            Log.d(TAG, msg);
                            Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show();
                            // if timeout, adding address may have triggered a change to the profile,
                            // therefore refresh cached profile so at least if user leaves page and
                            // re-enters they have a chance of succeeding
                            new ProfileDetails().refreshProfile(null);
                        }
                    });
        }
    }


    /** handles order submission */
    @Override
    protected void onSubmit() {

        com.staples.mobile.common.access.easyopen.model.member.Address billingAddress = ProfileDetails.getAddress(billingAddressId);
        final CCDetails profilePaymentMethod = ProfileDetails.getPaymentMethod(paymentMethodId);

        // make sure necessary selections have been made
        if (profilePaymentMethod == null) {
            Toast.makeText(activity, R.string.payment_method_required, Toast.LENGTH_LONG).show();
            return;
        }
        if (billingAddress == null) {
            Toast.makeText(activity, R.string.billing_address_required, Toast.LENGTH_LONG).show();
            return;
        }

        // first add billing address to the cart, then add payment method, then submit
        secureApi.addBillingAddressToCart(new BillingAddress(billingAddress),
                RECOMMENDATION, STORE_ID, LOCALE, CLIENT_ID, new Callback<AddressValidationAlert>() {
                    @Override
                    public void success(AddressValidationAlert precheckoutResponse, Response response) {

                        // next add payment method to cart
                        showProgressIndicator();
                        PaymentMethod cartPaymentMethod = new PaymentMethod(profilePaymentMethod);
                        secureApi.addPaymentMethodToCart(cartPaymentMethod, RECOMMENDATION, STORE_ID, LOCALE, CLIENT_ID,
                                new Callback<PaymentMethodResponse>() {
                                    @Override
                                    public void success(PaymentMethodResponse paymentMethodResponse, Response response) {
                                        // finally, upon payment method success, submit the order
                                        submitOrder(null);
                                    }

                                    @Override
                                    public void failure(RetrofitError retrofitError) {
                                        hideProgressIndicator();
                                        Toast.makeText(activity, "Payment Error: " + ApiError.getErrorMessage(retrofitError), Toast.LENGTH_LONG).show();
                                    }
                                }
                        );
                    }

                    @Override
                    public void failure(RetrofitError retrofitError) {
                        String msg = "Address error: " + ApiError.getErrorMessage(retrofitError);
                        Log.d(TAG, msg);
                        Toast.makeText(activity, msg, Toast.LENGTH_LONG).show();
                        hideProgressIndicator();
                    }
                });
    }


    @Override
    public void onClick(View view) {
        super.onClick(view);

        switch(view.getId()) {
            case R.id.shipping_addresses:
                activity.selectProfileAddressesFragment(new ProfileDetails.AddressSelectionListener() {
                    public void onAddressSelected(String id) {
                        Bundle args = RegisteredCheckoutFragment.this.getArguments();
                        args.putString(BUNDLE_PARAM_SHIPPING_ADDR_ID, id);
                        args.putString(BUNDLE_PARAM_SHIPPING_CHARGE, null); // set these to null to force new precheckout step
                        args.putString(BUNDLE_PARAM_TAX, null);             // set these to null to force new precheckout step
                        activity.selectFragment(RegisteredCheckoutFragment.this, MainActivity.Transition.NONE, true);
                    }
                }, shippingAddressId);
                break;
            case R.id.payment_methods:
                activity.selectProfileCreditCardsFragment(new ProfileDetails.PaymentMethodSelectionListener() {
                    public void onPaymentMethodSelected(String id) {
                        Bundle args = RegisteredCheckoutFragment.this.getArguments();
                        args.putString(BUNDLE_PARAM_PAYMENT_METHOD_ID, id);
                        activity.selectFragment(RegisteredCheckoutFragment.this, MainActivity.Transition.NONE, true);
                    }
                }, paymentMethodId);
                break;
            case R.id.billing_addresses:
                activity.selectProfileAddressesFragment(new ProfileDetails.AddressSelectionListener() {
                    public void onAddressSelected(String id) {
                        Bundle args = RegisteredCheckoutFragment.this.getArguments();
                        args.putString(BUNDLE_PARAM_BILLING_ADDR_ID, id);
                        activity.selectFragment(RegisteredCheckoutFragment.this, MainActivity.Transition.NONE, true);
                    }
                }, billingAddressId);
                break;
        }
    }


    /** formats payment method for display in widget */
    private String formatPaymentMethod(CCDetails paymentMethod) {
        StringBuilder b = new StringBuilder();
        if (paymentMethod != null) {
            b.append(paymentMethod.getCardType()).append(" ending in ")
                    .append(paymentMethod.getCardNumber().substring(paymentMethod.getCardNumber().length() - 4));
        }
        return b.toString();
    }

    /** formats address for display in widget */
    private String formatAddress(com.staples.mobile.common.access.easyopen.model.member.Address address) {
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
}
