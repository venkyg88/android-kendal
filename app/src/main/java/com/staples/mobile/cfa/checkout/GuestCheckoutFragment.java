/*
 * Copyright (c) 2014 Staples, Inc. All rights reserved.
 */

package com.staples.mobile.cfa.checkout;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.staples.mobile.cfa.R;
import com.staples.mobile.cfa.profile.CreditCard;
import com.staples.mobile.cfa.widget.AddressBlock;
import com.staples.mobile.common.access.easyopen.model.cart.BillingAddress;
import com.staples.mobile.common.access.easyopen.model.cart.PaymentMethod;
import com.staples.mobile.common.access.easyopen.model.cart.ShippingAddress;

public class GuestCheckoutFragment extends CheckoutFragment implements AddressBlock.OnDoneListener, CompoundButton.OnCheckedChangeListener {

    private static final String TAG = GuestCheckoutFragment.class.getSimpleName();

    AddressBlock shippingAddrBlock;
    AddressBlock billingAddrBlock;
    View paymentMethodLayoutVw;
    Switch useShipAddrAsBillingAddrSwitch;
    ImageView cardImage;
    EditText cardNumberVw;
    EditText expirationDateVw;
    EditText cidVw;

    private boolean shippingAddrNeedsApplying = true;
    private boolean billingAddrNeedsApplying = true;

    /**
     * Create a new instance of GuestCheckoutFragment that will be initialized
     * with the given arguments.
     */
    public static CheckoutFragment newInstance(float couponsRewardsAmount, float itemSubtotal, float preTaxSubtotal, String deliveryRange) {
        CheckoutFragment f = new GuestCheckoutFragment();
        f.setArguments(createInitialBundle(couponsRewardsAmount, itemSubtotal, preTaxSubtotal, deliveryRange));
        return f;
    }

    /** specifies layout for variable entry area */
    @Override
    protected int getEntryLayoutId() {
        return R.layout.checkout_guest_entry;
    }

    /** initializes variable entry area of checkout screen */
    @Override
    protected void initEntryArea(View frame) {
        shippingAddrBlock = (AddressBlock) frame.findViewById(R.id.shipping_addr_layout);
        shippingAddrBlock.init(true, true);
        shippingAddrBlock.setOnDoneListener(this);
        billingAddrBlock = (AddressBlock) frame.findViewById(R.id.billing_addr_layout);
        billingAddrBlock.init(true, false);
        billingAddrBlock.setOnDoneListener(this);

        paymentMethodLayoutVw = frame.findViewById(R.id.payment_method_layout);
        cardNumberVw = (EditText) paymentMethodLayoutVw.findViewById(R.id.cardNumber);
        cardImage = (ImageView) paymentMethodLayoutVw.findViewById(R.id.card_image);
        expirationDateVw = (EditText)paymentMethodLayoutVw.findViewById(R.id.expirationDate);
        cidVw = (EditText)paymentMethodLayoutVw.findViewById(R.id.cid);

        frame.findViewById(R.id.billing_addr_heading).setVisibility(View.GONE);
        billingAddrBlock.setVisibility(View.GONE);

        // hide imported views' Save buttons
        paymentMethodLayoutVw.findViewById(R.id.addCCBtn).setVisibility(View.GONE);
        paymentMethodLayoutVw.findViewById(R.id.cancelCCBtn).setVisibility(View.GONE);

        TextView.OnEditorActionListener paymentMethodCompletionListener = new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                // note that exp date will only have a DONE action if there's no CID, otherwise has NEXT action
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    if (useShipAddrAsBillingAddrSwitch.isChecked()) {
                        applyAddressesAndPrecheckout();
                    }
                }
                return false; // pass on to other listeners.
            }
        };

        expirationDateVw.setOnEditorActionListener(paymentMethodCompletionListener);
        cidVw.setOnEditorActionListener(paymentMethodCompletionListener);

        expirationDateVw.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(start == 1 && before < start) {
                    expirationDateVw.setText(s+"/");
                    expirationDateVw.setSelection(expirationDateVw.getText().length());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }});

        cardNumberVw.setOnEditorActionListener(
                new EditText.OnEditorActionListener() {

                    @Override
                    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                        if (actionId == EditorInfo.IME_ACTION_NEXT) {
                            CreditCard.Type ccType = CreditCard.Type.detect(cardNumberVw.getText().toString());
                            cardImage.setImageResource(ccType.getImageResource());
                            expirationDateVw.setVisibility(View.VISIBLE);
                            if (ccType.isCidUsed()) {
                                cidVw.setVisibility(View.VISIBLE);
                                expirationDateVw.setImeOptions(EditorInfo.IME_ACTION_NEXT);
                            }
                            expirationDateVw.requestFocus();
                            return true; // consume.
                        }
                        return false; // pass on to other listeners.
                    }
                });

        cardNumberVw.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (MotionEvent.ACTION_UP == event.getAction()) {
                    cardNumberVw.getText().clear();
                    cardImage.setImageResource(0);
                }
                return false; // return is important...
            }
        });

        // add listener to billing addr toggle button switch
        useShipAddrAsBillingAddrSwitch = (Switch) frame.findViewById(R.id.useShipAddrAsBillingAddr_switch);
        useShipAddrAsBillingAddrSwitch.setChecked(true);
        useShipAddrAsBillingAddrSwitch.setOnCheckedChangeListener(this);
    }

    public void onDone(AddressBlock addressBlock) {
        if (addressBlock==shippingAddrBlock) {
            shippingAddrNeedsApplying = true;
            if (useShipAddrAsBillingAddrSwitch.isChecked()) {
                billingAddrNeedsApplying = true;
            }
            applyAddressesAndPrecheckout();
        }

        else if (addressBlock==billingAddrBlock) {
            billingAddrNeedsApplying = true;
            applyAddressesAndPrecheckout();
        }
    }

    /** implements CompoundButton.OnCheckedChangeListener */
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        int visibility = isChecked? View.GONE: View.VISIBLE;
        getView().findViewById(R.id.billing_addr_heading).setVisibility(visibility);
        billingAddrBlock.setVisibility(visibility);
        billingAddrNeedsApplying = true;
//        applyAddressesAndPrecheckout();
    }

    /** gets shipping address from user's entries */
    private ShippingAddress getShippingAddress() {
        return(shippingAddrBlock.getShippingAddress());
    }

    /** gets billing address from user's entries */
    private BillingAddress getBillingAddress() {
        if (useShipAddrAsBillingAddrSwitch.isChecked()) {
            return(new BillingAddress(shippingAddrBlock.getShippingAddress()));
        } else {
            return(new BillingAddress(billingAddrBlock.getShippingAddress()));
        }
    }

    private boolean validateRequired(TextView view) {
        if (view==null || view.getVisibility()!=View.VISIBLE) return(true);

        String text = view.getText().toString().trim();
        if (text.length()==0) {
            view.setError(getActivity().getResources().getString(R.string.required));
            return false;
        }
        return true;
    }


    /** gets payment method from user's entries */
    private PaymentMethod getPaymentMethod() {
        boolean valid = true;
        valid &= validateRequired(cardNumberVw);
        valid &= validateRequired(expirationDateVw);
        CreditCard.Type ccType = CreditCard.Type.detect(cardNumberVw.getText().toString());
        if (ccType.isCidUsed()) {
            valid &= validateRequired(cidVw);
        }

        if (valid) {
            PaymentMethod paymentMethod = new PaymentMethod();
            paymentMethod.setSaveCardIndicator("Y");
            paymentMethod.setCardNumber(cardNumberVw.getText().toString());
            paymentMethod.setCardType(ccType.getName());
            paymentMethod.setCardExpirationMonth(expirationDateVw.getText().toString().substring(0,2));
            paymentMethod.setCardExpirationYear("20" +expirationDateVw.getText().toString().substring(3,5));
            if (ccType.isCidUsed()) {
                paymentMethod.setCardVerificationCode(cidVw.getText().toString());
            }
            return paymentMethod;
        }
        return null;
    }

    private boolean readyForPrecheckout() {
        boolean shippingAddrReady = shippingAddrBlock.validate();
        boolean billingAddrReady = useShipAddrAsBillingAddrSwitch.isChecked()? shippingAddrReady : billingAddrBlock.validate();
        boolean paymentMethodReady = (cidVw.getVisibility() == View.VISIBLE)?
                !TextUtils.isEmpty(cidVw.getText()) : !TextUtils.isEmpty(expirationDateVw.getText());
        return (shippingAddrReady && billingAddrReady && paymentMethodReady);
    }

    private void applyAddressesAndPrecheckout() {
        if (!readyForPrecheckout()) {
            // reset shipping/tax info if already calculated and showing
            resetShippingAndTax();
            return;
        }

        activity.hideSoftKeyboard(cardNumberVw);

        // add shipping address to cart if necessary, then billing address and precheckout
        if (shippingAddrNeedsApplying) {
            ShippingAddress shippingAddress = getShippingAddress();
            if (shippingAddress == null) {
                activity.showErrorDialog(R.string.required_fields);
            } else {
                showProgressIndicator();
                CheckoutApiManager.applyShippingAddress(shippingAddress, new CheckoutApiManager.ApplyAddressCallback() {
                    @Override
                    public void onApplyAddressComplete(String addressId, String errMsg, String infoMsg) {
                        hideProgressIndicator();

                        // if success
                        if (errMsg == null) {
                            shippingAddrNeedsApplying = false;

                            if (infoMsg != null) {
                                activity.showErrorDialog("Shipping address alert: " + infoMsg);
                            }

                            // now apply billing address
                            applyBillingAddressIfNeededAndPrecheckout();

                        } else {
                            // if shipping and tax already showing, need to hide them
                            resetShippingAndTax();

                            activity.showErrorDialog(errMsg);
                            Log.d(TAG, errMsg);
                        }
                    }
                });
            }
        } else {
            applyBillingAddressIfNeededAndPrecheckout();
        }
    }

    private void applyBillingAddressIfNeededAndPrecheckout() {

        // apply billing address to cart if necessary
        if (billingAddrNeedsApplying) {
            BillingAddress billingAddress = getBillingAddress();
            if (billingAddress == null) {
                activity.showErrorDialog(R.string.required_fields);
            } else {
                showProgressIndicator();
                CheckoutApiManager.applyBillingAddress(billingAddress, new CheckoutApiManager.ApplyAddressCallback() {
                    @Override
                    public void onApplyAddressComplete(String addressId, String errMsg, String infoMsg) {
                        hideProgressIndicator();

                        // if success
                        if (errMsg == null) {
                            billingAddrNeedsApplying = false;

                            if (infoMsg != null) {
                                activity.showErrorDialog("Billing address alert: " + infoMsg);
                            }

                            // do precheckout
                            startPrecheckoutIfReady();

                        } else {
                            // if shipping and tax already showing, need to hide them
                            resetShippingAndTax();

                            activity.showErrorDialog(errMsg);
                            Log.d(TAG, errMsg);
                        }
                    }
                });
            }
        } else {
            // do precheckout
            startPrecheckoutIfReady();
        }
    }

    private void startPrecheckoutIfReady() {
        if (!shippingAddrNeedsApplying && !billingAddrNeedsApplying) {
            startPrecheckout();
        }
    }

    /** handles order submission */
    @Override
    protected void onSubmit() {
        submitPaymentMethodAndOrder();
    }

    private void submitPaymentMethodAndOrder() {

        final PaymentMethod paymentMethod = getPaymentMethod();
        if (paymentMethod==null) {
            activity.showErrorDialog(R.string.payment_method_required);
            return;
        }

        showProgressIndicator();
        // encrypt and apply payment method
        CheckoutApiManager.encryptAndApplyPaymentMethod(paymentMethod, new CheckoutApiManager.ApplyPaymentMethodCallback() {
            @Override
            public void onApplyPaymentMethodComplete(String paymentMethodId, String authorized, String errMsg) {
                hideProgressIndicator();

                // if success
                if (errMsg == null) {

                    // submit the order
                    submitOrder(paymentMethod.getCardVerificationCode(), shippingAddrBlock.getEmailAddress());

                } else {
                    activity.showErrorDialog(errMsg);
                    Log.d(TAG, errMsg);
                }
            }
        });
    }
}
