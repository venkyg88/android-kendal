/*
 * Copyright (c) 2014 Staples, Inc. All rights reserved.
 */

package com.staples.mobile.cfa.checkout;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.staples.mobile.cfa.R;
import com.staples.mobile.cfa.MainActivity;
import com.staples.mobile.cfa.profile.CreditCard;
import com.staples.mobile.cfa.profile.ProfileDetails;
import com.staples.mobile.common.access.easyopen.model.cart.BillingAddress;
import com.staples.mobile.common.access.easyopen.model.cart.PaymentMethod;
import com.staples.mobile.common.access.easyopen.model.cart.ShippingAddress;
import com.staples.mobile.common.access.easyopen.model.member.CCDetails;
import com.staples.mobile.common.access.easyopen.model.member.Member;

import java.util.List;


public class RegisteredCheckoutFragment extends CheckoutFragment implements View.OnClickListener {
    private static final String TAG = RegisteredCheckoutFragment.class.getSimpleName();


    // additional bundle param keys
    public static final String BUNDLE_PARAM_SHIPPING_ADDR_ID = "shippingAddrId";
    public static final String BUNDLE_PARAM_BILLING_ADDR_ID = "billingAddrId";
    public static final String BUNDLE_PARAM_PAYMENT_METHOD_ID = "paymentMethodId";


    private TextView shippingNameVw;
    private TextView shippingAddrVw;
    private TextView paymentMethodVw;
    private ImageView paymentMethodImage;
    private TextView billingNameVw;
    private TextView billingAddrVw;

    // profile selections
    String shippingAddressId;
    String paymentMethodId;
    String billingAddressId;

    /**
     * Create a new instance of RegisteredCheckoutFragment that will be initialized
     * with the given arguments. Used when opening a fresh checkout session from the cart.
     */
    public static CheckoutFragment newInstance(float couponsRewardsAmount, float itemSubtotal, float preTaxSubtotal, String deliveryRange) {
        CheckoutFragment f = new RegisteredCheckoutFragment();
        createInitialBundle(couponsRewardsAmount, itemSubtotal, preTaxSubtotal, deliveryRange);
        f.setArguments(createInitialBundle(couponsRewardsAmount, itemSubtotal, preTaxSubtotal, deliveryRange));
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
        shippingNameVw = (TextView) view.findViewById(R.id.checkout_shipping_name);
        shippingAddrVw = (TextView) view.findViewById(R.id.checkout_shipping_addr);
        paymentMethodVw = (TextView) view.findViewById(R.id.checkout_payment_method);
        paymentMethodImage = (ImageView) view.findViewById(R.id.card_image);
        billingNameVw = (TextView) view.findViewById(R.id.checkout_billing_name);
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
            shippingNameVw.setText(formatAddressName(shippingAddress));
            shippingAddrVw.setText(formatAddress(shippingAddress));
        }
        if (paymentMethod != null) {
            paymentMethodVw.setText(formatPaymentMethod(paymentMethod));
            paymentMethodImage.setImageResource(CreditCard.Type.matchOnApiName(paymentMethod.getCardType()).getImageResource());
        }
        if (billingAddress != null) {
            billingNameVw.setText(formatAddressName(billingAddress));
            billingAddrVw.setText(formatAddress(billingAddress));
        }

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
        final com.staples.mobile.common.access.easyopen.model.member.Address profileAddress = ProfileDetails.getAddress(shippingAddressId);
        if (profileAddress != null) {
            showProgressIndicator();
            CheckoutApiManager.applyShippingAddress(new ShippingAddress(profileAddress), new CheckoutApiManager.ApplyAddressCallback() {
                    @Override
                    public void onApplyAddressComplete(String addressId, String errMsg, String infoMsg) {
                        hideProgressIndicator();

                        // if success
                        if (errMsg == null) {

                            // applying the shipping address actually modifies the id on the server, so need to fix everything up
                            Bundle checkoutBundle = RegisteredCheckoutFragment.this.getArguments();
                            String oldId = shippingAddressId;
                            String newId = addressId;
                            profileAddress.setAddressId(newId); // fix id in the profile
                            shippingAddressId = newId; // fix our local id
                            checkoutBundle.putString(BUNDLE_PARAM_SHIPPING_ADDR_ID, newId); // fix id in bundle
                            if (oldId.equals(billingAddressId)) {
                                billingAddressId = newId; // fix our local id
                                checkoutBundle.putString(BUNDLE_PARAM_BILLING_ADDR_ID, newId); // fix id in bundle
                            }

                            startPrecheckout();

                        } else {
                            // if shipping and tax already showing, need to hide them
                            resetShippingAndTax();

                            showErrorDialog(errMsg);
                            Log.d(TAG, errMsg);
                        }
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
            showErrorDialog(R.string.payment_method_required);
            return;
        }
        if (billingAddress == null) {
            showErrorDialog(R.string.billing_address_required);
            return;
        }

        // first add billing address to the cart, then add payment method, then submit
        showProgressIndicator();
        CheckoutApiManager.applyBillingAddress(new BillingAddress(billingAddress), new CheckoutApiManager.ApplyAddressCallback() {
            @Override
            public void onApplyAddressComplete(String addressId, String errMsg, String infoMsg) {
                hideProgressIndicator();

                // if success
                if (errMsg == null) {
                    showProgressIndicator();
                    CheckoutApiManager.applyPaymentMethod(new PaymentMethod(profilePaymentMethod), new CheckoutApiManager.ApplyPaymentMethodCallback() {
                        @Override
                        public void onApplyPaymentMethodComplete(String paymentMethodId, String authorized, String errMsg) {
                            hideProgressIndicator();
                            // if success
                            if (errMsg == null) {
                                // finally, upon payment method success, submit the order
                                submitOrder(null, ProfileDetails.getMember().getEmailAddress());
                            } else {
                                showErrorDialog(errMsg);
                                Log.d(TAG, errMsg);
                            }
                        }
                    });
                } else {
                    showErrorDialog(errMsg);
                    Log.d(TAG, errMsg);
                }
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
            String cardNumber = paymentMethod.getCardNumber();
            if (cardNumber.length() > 4) {
                cardNumber = cardNumber.substring(cardNumber.length() - 4);
            }
            b.append("Card ending in ").append(cardNumber);
        }
        return b.toString();
    }

    /** formats address for display in widget */
    private String formatAddressName(com.staples.mobile.common.access.easyopen.model.member.Address address) {
        StringBuilder b = new StringBuilder();
        if (address != null) {
            if (!TextUtils.isEmpty(address.getFirstname())) {
                b.append(address.getFirstname());
                if (!TextUtils.isEmpty(address.getLastname())) {
                    b.append(" ").append(address.getLastname());
                }
            }
        }
        return b.toString();
    }

    /** formats address for display in widget */
    private String formatAddress(com.staples.mobile.common.access.easyopen.model.member.Address address) {
        StringBuilder b = new StringBuilder();
        if (address != null) {
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
                if (!TextUtils.isEmpty(address.getZipcode())) {
                    b.append(" ").append(address.getZipcode());
                }
            }
        }
        return b.toString();
    }
}
