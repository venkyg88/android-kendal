package com.staples.mobile.cfa.checkout;

import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.staples.mobile.cfa.R;
import com.staples.mobile.cfa.profile.CcNumberInputFilter;
import com.staples.mobile.cfa.profile.CreditCard;
import com.staples.mobile.cfa.widget.ActionBar;
import com.staples.mobile.cfa.widget.AddressBlock;
import com.staples.mobile.common.access.easyopen.model.cart.BillingAddress;
import com.staples.mobile.common.access.easyopen.model.cart.PaymentMethod;
import com.staples.mobile.common.access.easyopen.model.cart.ShippingAddress;
import com.staples.mobile.common.analytics.Tracker;

import java.util.Calendar;

public class GuestCheckoutFragment extends CheckoutFragment implements AddressBlock.OnDoneListener, CompoundButton.OnCheckedChangeListener {

    private static final String TAG = GuestCheckoutFragment.class.getSimpleName();

    AddressBlock shippingAddrBlock;
    AddressBlock billingAddrBlock;
    View paymentMethodLayoutVw;
    Switch useShipAddrAsBillingAddrSwitch;
    ImageView cardImage;
    EditText cardNumberVw;
    EditText expirationMonthVw;
    EditText expirationYearVw;

//    EditText expirationDateVw;
    EditText cidVw;

    private boolean shippingAddrNeedsApplying = true;
    private boolean billingAddrNeedsApplying = true;

    /**
     * Create a new instance of GuestCheckoutFragment
     */
    public static CheckoutFragment newInstance() {
        CheckoutFragment f = new GuestCheckoutFragment();
        f.setArguments(new Bundle());
        return f;
    }

    @Override
    public void onResume() {
        super.onResume();
        ActionBar.getInstance().setConfig(ActionBar.Config.COGUEST);
        Tracker.getInstance().trackStateForCheckoutReviewAndPay(false, false); // analytics
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
        shippingAddrBlock.init(true);
        shippingAddrBlock.setOnDoneListener(this);
        billingAddrBlock = (AddressBlock) frame.findViewById(R.id.billing_addr_layout);
        billingAddrBlock.init(false);
        billingAddrBlock.setOnDoneListener(this);

        paymentMethodLayoutVw = frame.findViewById(R.id.payment_method_layout);
        cardNumberVw = (EditText) paymentMethodLayoutVw.findViewById(R.id.cardNumber);
        cardImage = (ImageView) paymentMethodLayoutVw.findViewById(R.id.card_image);
        expirationMonthVw = (EditText) paymentMethodLayoutVw.findViewById(R.id.expiration_month);
        expirationYearVw = (EditText) paymentMethodLayoutVw.findViewById(R.id.expiration_year);

        expirationMonthVw.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                String input = editable.toString();
                if (editable.length() == 1) {
                    int month = Integer.parseInt(input);
                    if (month > 1) {
                        expirationMonthVw.setText("0" + expirationMonthVw.getText().toString());
                        expirationYearVw.requestFocus();
                    }

                } else if (editable.length() == 2) {
                    int month = Integer.parseInt(input);
                    if (month <= 12) {
                    } else {
                        activity.showErrorDialog("Please check the expiration month");
                    }
                } else {
                }

            }
        });

        expirationYearVw.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                String input = editable.toString();
                if (editable.length() == 2) {
                    Calendar calendar = Calendar.getInstance();
                    int currentYear = calendar.get(Calendar.YEAR) % 100;
                    int year = Integer.parseInt(input);

                    if (year < currentYear) {
                        activity.showErrorDialog("Please check the expiration year");
                    }
                }
            }
        });

     // TODO commented out code
//        expirationDateVw = (EditText)paymentMethodLayoutVw.findViewById(R.id.expirationDate);
        cidVw = (EditText)paymentMethodLayoutVw.findViewById(R.id.cid);
        cidVw.setVisibility(View.VISIBLE); // set initially visible, hide later if not applicable to card type (as per Joe Raffone)
        expirationYearVw.setImeOptions(EditorInfo.IME_ACTION_NEXT);

        // TODO commented out code
        //expirationDateVw.setImeOptions(EditorInfo.IME_ACTION_NEXT);

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

        // TODO commented out code
        //expirationDateVw.setOnEditorActionListener(paymentMethodCompletionListener);
        cidVw.setOnEditorActionListener(paymentMethodCompletionListener);

        cardNumberVw.setFilters(new InputFilter[]{new CcNumberInputFilter()});

        // TODO commented out code
        //expirationDateVw.setFilters(new InputFilter[]{new ExpiryDateInputFilter()});

        // add listener to billing addr toggle button switch
        useShipAddrAsBillingAddrSwitch = (Switch) frame.findViewById(R.id.useShipAddrAsBillingAddr_switch);
        useShipAddrAsBillingAddrSwitch.setChecked(true);
        useShipAddrAsBillingAddrSwitch.setOnCheckedChangeListener(this);

        // focus listener for CC and shipping addr
        View.OnFocusChangeListener focusListener = new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    if (v.getId() == R.id.firstName) {
                        Tracker.getInstance().trackActionForCheckoutEnterAddress(); // analytics
                    } else if (v.getId() == R.id.cardNumber) {
                        Tracker.getInstance().trackActionForCheckoutEnterPayment(); // analytics
                    }
//                    else if (v.getId() == R.id.expirationDate) {
//                        // loss of focus on CC number isn't consistent, so handle gain of focus on exp date too
//                        handleCardNumberChange();
//                    }
                } else  {
                    // when CC number loses focus, evaluate card type and show/hide CID
                    if (v.getId() == R.id.cardNumber) {
                        handleCardNumberChange();
                    }
                }
            }
        };
        shippingAddrBlock.findViewById(R.id.firstName).setOnFocusChangeListener(focusListener);
        cardNumberVw.setOnFocusChangeListener(focusListener);

        // TODO commented out code
        // expirationDateVw.setOnFocusChangeListener(focusListener);
    }

    private void handleCardNumberChange() {
        String cardNumber = cardNumberVw.getText().toString();
        if (!TextUtils.isEmpty(cardNumber)) {
            CreditCard.Type ccType = CreditCard.Type.detect(cardNumberVw.getText().toString().replaceAll(" ", ""));
            cardImage.setImageResource(ccType.getImageResource());
            if (!ccType.isCidUsed()) {
                cidVw.setVisibility(View.GONE);
                // TODO commented out code
                //expirationDateVw.setImeOptions(EditorInfo.IME_ACTION_DONE);
            } else {
                cidVw.setVisibility(View.VISIBLE);
                // TODO commented out code
                //expirationDateVw.setImeOptions(EditorInfo.IME_ACTION_NEXT);
            }
        }
    }

    public void onNext(AddressBlock addressBlock) {
        ShippingAddress shippingAddress = addressBlock.getShippingAddress();
        if (!TextUtils.isEmpty(shippingAddress.getDeliveryZipCode())) {
            onDone(addressBlock, addressBlock.validate());
        }
    }

    public void onDone(AddressBlock addressBlock, boolean valid) {
        if (addressBlock == shippingAddrBlock) {
            shippingAddrNeedsApplying = true;
            if (useShipAddrAsBillingAddrSwitch.isChecked()) {
                billingAddrNeedsApplying = true;
            }
        } else if (addressBlock == billingAddrBlock) {
            billingAddrNeedsApplying = true;
        }
        if (!valid) {
            String errMsg = activity.getResources().getString(R.string.required_fields);
            Tracker.getInstance().trackActionForCheckoutFormErrors(errMsg); // analytics
            activity.showErrorDialog(errMsg);
        } else {
            applyAddressesAndPrecheckout();
        }
    }

    /** implements CompoundButton.OnCheckedChangeListener */
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        int visibility = isChecked? View.GONE: View.VISIBLE;
        getView().findViewById(R.id.billing_addr_heading).setVisibility(visibility);
        billingAddrBlock.setVisibility(visibility);
        billingAddrNeedsApplying = true;
        billingAddrBlock.requestFocus();
        if (isChecked || !TextUtils.isEmpty(billingAddrBlock.getShippingAddress().getDeliveryZipCode())) {
            applyAddressesAndPrecheckout();
        }
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
            view.setError(activity.getResources().getString(R.string.required));
            return false;
        }
        return true;
    }

    /** gets payment method from user's entries */
    private PaymentMethod getPaymentMethod() {
        boolean valid = true;
        valid &= validateRequired(cardNumberVw);
        // TODO commented out code
//        valid &= validateRequired(expirationDateVw);
        CreditCard.Type ccType = CreditCard.Type.detect(cardNumberVw.getText().toString().replaceAll(" ", ""));
        if (ccType.isCidUsed()) {
            valid &= validateRequired(cidVw);
        }
        // TODO commented out code

//        if (valid) {
//            boolean dateValid = DateUtils.validateCreditCardExpDate(expirationDateVw);
//            if ( ! dateValid) {
//                String errMsg = activity.getResources().getString(R.string.expiration_date_error);
//                expirationDateVw.setError(errMsg);
//                valid = false;
//            }
//        }

        if (valid) {
            PaymentMethod paymentMethod = new PaymentMethod();
            paymentMethod.setSaveCardIndicator("Y");
            paymentMethod.setCardNumber(cardNumberVw.getText().toString().replaceAll(" ", ""));
            paymentMethod.setCardType(ccType.getName());
            paymentMethod.setCardExpirationMonth(expirationMonthVw.getText().toString().substring(0,2));
            paymentMethod.setCardExpirationYear("20" +expirationYearVw.getText().toString().substring(3,5));
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
                !TextUtils.isEmpty(cidVw.getText()) : !TextUtils.isEmpty(cidVw.getText());
        // TODO commented out code
//                !TextUtils.isEmpty(expirationDateVw.getText());
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
                String errMsg = activity.getResources().getString(R.string.required_fields);
                Tracker.getInstance().trackActionForCheckoutFormErrors(errMsg); // analytics
                activity.showErrorDialog(errMsg);
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
                                Tracker.getInstance().trackActionForCheckoutFormErrors("Shipping address alert: " + infoMsg); // analytics
                                activity.showErrorDialog("Shipping address alert: " + infoMsg);
                            }

                            // now apply billing address
                            applyBillingAddressIfNeededAndPrecheckout();

                        } else {
                            // if shipping and tax already showing, need to hide them
                            resetShippingAndTax();

                            Tracker.getInstance().trackActionForCheckoutFormErrors(errMsg); // analytics
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
                String errMsg = activity.getResources().getString(R.string.required_fields);
                Tracker.getInstance().trackActionForCheckoutFormErrors(errMsg); // analytics
                activity.showErrorDialog(errMsg);
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
                                Tracker.getInstance().trackActionForCheckoutFormErrors("Billing address alert: " + infoMsg); // analytics
                                activity.showErrorDialog("Billing address alert: " + infoMsg);
                            }

                            // do precheckout
                            startPrecheckoutIfReady();

                        } else {
                            // if shipping and tax already showing, need to hide them
                            resetShippingAndTax();

                            Tracker.getInstance().trackActionForCheckoutFormErrors(errMsg); // analytics
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
            String errMsg = activity.getResources().getString(R.string.payment_method_required);
            Tracker.getInstance().trackActionForCheckoutFormErrors(errMsg); // analytics
            activity.showErrorDialog(errMsg);
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
                    submitOrder(paymentMethod, shippingAddrBlock.getEmailAddress());

                } else {
                    Tracker.getInstance().trackActionForCheckoutFormErrors(errMsg); // analytics
                    activity.showErrorDialog(errMsg);
                    Log.d(TAG, errMsg);
                }
            }
        });
    }
}
