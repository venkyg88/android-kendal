package app.staples.mobile.cfa.checkout;

import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.staples.mobile.common.access.easyopen.model.cart.BillingAddress;
import com.staples.mobile.common.access.easyopen.model.cart.PaymentMethod;
import com.staples.mobile.common.access.easyopen.model.cart.ShippingAddress;
import com.staples.mobile.common.analytics.Tracker;

import java.util.Calendar;

import app.staples.R;
import app.staples.mobile.cfa.profile.CcNumberInputFilter;
import app.staples.mobile.cfa.profile.CreditCard;
import app.staples.mobile.cfa.widget.ActionBar;
import app.staples.mobile.cfa.widget.AddressBlock;

public class GuestCheckoutFragment extends CheckoutFragment implements AddressBlock.OnDoneListener,
        CompoundButton.OnCheckedChangeListener, TextWatcher, View.OnFocusChangeListener,
        TextView.OnEditorActionListener, View.OnClickListener{

    private static final String TAG = GuestCheckoutFragment.class.getSimpleName();

    AddressBlock shippingAddrBlock;
    AddressBlock billingAddrBlock;
    View billingAddrHeadingVw;
    View paymentMethodLayoutVw;
    Switch useShipAddrAsBillingAddrSwitch;
    ImageView cardImage;
    EditText cardNumberVw;
    EditText expirationMonthVw;
    EditText expirationYearVw;
    EditText cidVw;
    TextView zipCodeVw;
    View submissionLayout;
    TextView preCheckOutValidateBtn;
    View guestPaymentLayoutVw;
    View billingSwitchSelectLayoutVw;
    View preCheckoutShippingLayoutVw;
    View preCheckoutBillingLayoutVw;
    TextView shippingAddressTv;
    TextView billingAddressTv;
    TextView billingAddressLbl;

    private boolean shippingAddrNeedsApplying = true;
    private boolean billingAddrNeedsApplying = true;

    // cache guest checkout entries
    private static ShippingAddress shippingAddressCache;
    private static ShippingAddress billingAddressCache;
    private static boolean useShippingAsBillingCache = true;

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

    @Override
    public void onPause() {
        // cache current address entries
        shippingAddressCache = shippingAddrBlock.getShippingAddress();
        billingAddressCache = billingAddrBlock.getShippingAddress();
        useShippingAsBillingCache = useShipAddrAsBillingAddrSwitch.isChecked();
        super.onPause();
    }

    /** specifies layout for variable entry area */
    @Override
    protected int getEntryLayoutId() {
        return R.layout.checkout_guest_entry;
    }

    /** initializes variable entry area of checkout screen */
    @Override
    protected void initEntryArea(View frame) {
        disableCheckoutButton(true);
        shippingAddrBlock = (AddressBlock) frame.findViewById(R.id.shipping_addr_layout);
        shippingAddrBlock.setOnDoneListener(this);
        billingAddrBlock = (AddressBlock) frame.findViewById(R.id.billing_addr_layout);
        billingAddrBlock.setOnDoneListener(this);
        billingAddrHeadingVw = frame.findViewById(R.id.billing_addr_heading);
        zipCodeVw = (TextView) shippingAddrBlock.findViewById(R.id.zipCode);
        preCheckOutValidateBtn = (TextView)frame.findViewById(R.id.precheckout_validate_button);
        guestPaymentLayoutVw = frame.findViewById(R.id.guest_payment_layout);
        billingSwitchSelectLayoutVw = frame.findViewById(R.id.billing_select_layout);
        preCheckoutShippingLayoutVw = frame.findViewById(R.id.shipping_addon_layout);
        preCheckoutBillingLayoutVw = frame.findViewById(R.id.billing_addon_layout);
        shippingAddressTv = (TextView)frame.findViewById(R.id.shipping_address_guest);
        billingAddressTv = (TextView)frame.findViewById(R.id.billing_address_guest);
        billingAddressLbl = (TextView)frame.findViewById(R.id.billing_addr_heading);

        paymentMethodLayoutVw = frame.findViewById(R.id.payment_method_layout);
        cardNumberVw = (EditText) paymentMethodLayoutVw.findViewById(R.id.cardNumber);
        cardImage = (ImageView) paymentMethodLayoutVw.findViewById(R.id.card_image);
        expirationMonthVw = (EditText) paymentMethodLayoutVw.findViewById(R.id.expiration_month);
        expirationYearVw = (EditText) paymentMethodLayoutVw.findViewById(R.id.expiration_year);
        cidVw = (EditText)paymentMethodLayoutVw.findViewById(R.id.cid);
        useShipAddrAsBillingAddrSwitch = (Switch) frame.findViewById(R.id.useShipAddrAsBillingAddr_switch);

        // initialize from cache BEFORE calling init on the address blocks to avoid autocomplete popping up
        if (shippingAddressCache != null) {
            shippingAddrBlock.setShippingAddress(shippingAddressCache);
        }
        if (billingAddressCache != null) {
            billingAddrBlock.setShippingAddress(billingAddressCache);
        }

        // initialize address blocks AFTER setting values from cache so that autocomplete is not triggered
        shippingAddrBlock.init(true);
        billingAddrBlock.init(false);

        // initialize autocomplete mode AFTER calling init to avoid NPE
        if (shippingAddressCache != null && !TextUtils.isEmpty(shippingAddressCache.getDeliveryCity())) {
            shippingAddrBlock.selectMode(false);
        }
        if (billingAddressCache != null && !TextUtils.isEmpty(billingAddressCache.getDeliveryCity())) {
            billingAddrBlock.selectMode(false);
        }


        // TODO: ideally the expiration date code should be encapsulated in a custom compound view,
        // but given the end-of-project rush, this will have to do

        cidVw.setVisibility(View.VISIBLE); // set initially visible, hide later if not applicable to card type (as per Joe Raffone)
        expirationYearVw.setImeOptions(EditorInfo.IME_ACTION_NEXT);

        billingAddrHeadingVw.setVisibility(View.GONE);
        billingAddrBlock.setVisibility(View.GONE);

//        TextView.OnEditorActionListener paymentMethodCompletionListener = this;
//        expirationYearVw.setOnEditorActionListener(paymentMethodCompletionListener);
//        cidVw.setOnEditorActionListener(paymentMethodCompletionListener);
        cardNumberVw.setFilters(new InputFilter[]{new InputFilter.LengthFilter(19), new CcNumberInputFilter()});
        // add listener to billing addr toggle button switch
        useShipAddrAsBillingAddrSwitch.setChecked(true);
        useShipAddrAsBillingAddrSwitch.setOnCheckedChangeListener(this);

        // if cached value is unchecked, fake a change to initialize state correctly
        if (!useShippingAsBillingCache) {
            useShipAddrAsBillingAddrSwitch.setChecked(false);
            onCheckedChanged(useShipAddrAsBillingAddrSwitch, false);
        }

        // focus listener for CC and shipping addr
        shippingAddrBlock.findViewById(R.id.firstName).setOnFocusChangeListener(this);
        cardNumberVw.setOnFocusChangeListener(this);
        cardNumberVw.addTextChangedListener(this);
        expirationMonthVw.addTextChangedListener(this);
        expirationMonthVw.setOnFocusChangeListener(this);
        expirationYearVw.addTextChangedListener(this);
        zipCodeVw.setOnFocusChangeListener(this);
        cidVw.setOnEditorActionListener(this);
        preCheckOutValidateBtn.setOnClickListener(this);
        preCheckoutShippingLayoutVw.setOnClickListener(this);
        preCheckoutBillingLayoutVw.setOnClickListener(this);
    }

    @Override
    public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
        // note that exp date will only have a DONE action if there's no CID, otherwise has NEXT action
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            if(textView.getId() == R.id.cid) {
                if(activity != null)
                activity.hideSoftKeyboard();
            }
            else {
                applyAddressesAndPrecheckout();
            }
        }
        return false; // pass on to other listeners.
    }

    @Override
    public void onFocusChange(View view, boolean hasFocus) {
        if (hasFocus) {
        if (view.getId() == R.id.firstName) {
            Tracker.getInstance().trackActionForCheckoutEnterAddress(); // analytics
        } else if (view.getId() == R.id.cardNumber) {
            Tracker.getInstance().trackActionForCheckoutEnterPayment(); // analytics
        } else if (view.getId() == R.id.expiration_month) {
            // loss of focus on CC number isn't consistent, so handle gain of focus on exp date too
            handleCardNumberChange();
            }
        } else  {
            // when CC number loses focus, evaluate card type and show/hide CID
            if (view.getId() == R.id.cardNumber) {
                handleCardNumberChange();
            }
            if(view.getId() == R.id.expiration_month) {
                CreditCard.Type ccType = CreditCard.Type.detect(cardNumberVw.getText().toString().replaceAll(" ", ""));
                if (ccType != CreditCard.Type.UNKNOWN) {
                    cardImage.setImageResource(ccType.getImageResource());
            }
        }
        }
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {}

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {}

    @Override
    public void afterTextChanged(Editable editable) {
        if(expirationMonthVw.getText().hashCode() == editable.hashCode()) {
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
                    expirationYearVw.requestFocus();
                } else {
                    activity.showErrorDialog("Please check the expiration month");
                }
            } else {
            }
        }
        if(expirationYearVw.getText().hashCode() == editable.hashCode()) {
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
    }

    private void handleCardNumberChange() {
        String cardNumber = cardNumberVw.getText().toString();
        if (!TextUtils.isEmpty(cardNumber)) {
            CreditCard.Type ccType = CreditCard.Type.detect(cardNumberVw.getText().toString().replaceAll(" ", ""));
            cardImage.setImageResource(ccType.getImageResource());
            if (!ccType.isCidUsed()) {
                cidVw.setVisibility(View.GONE);
                expirationYearVw.setImeOptions(EditorInfo.IME_ACTION_DONE);
            } else {
                cidVw.setVisibility(View.VISIBLE);
                expirationYearVw.setImeOptions(EditorInfo.IME_ACTION_NEXT);
            }
        }
    }

    public void onNext(AddressBlock addressBlock) {
        if (addressBlock == shippingAddrBlock) {
            shippingAddrNeedsApplying = true;
            if (useShipAddrAsBillingAddrSwitch.isChecked()) {
                billingAddrNeedsApplying = true;
            }
        } else if (addressBlock == billingAddrBlock) {
            billingAddrNeedsApplying = true;
        }
    }

    public void onDone(AddressBlock addressBlock, boolean valid) {
        onNext(addressBlock);
        applyAddressesAndPrecheckout();
    }

    /** implements CompoundButton.OnCheckedChangeListener */
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        int visibility = isChecked ? View.GONE: View.VISIBLE;
        int shippingVisibility = isChecked ? View.VISIBLE: View.GONE;
        billingAddrHeadingVw.setVisibility(visibility);
        billingAddrBlock.setVisibility(visibility);
        billingAddrNeedsApplying = true;
        if(!isChecked)billingAddrBlock.requestFocus();
        if(!shippingAddrBlock.validate()) {
            shippingAddrBlock.setVisibility(shippingVisibility);
            preCheckoutShippingLayoutVw.setVisibility(visibility);
            shippingAddressTv.setText(shippingAddrBlock.getShippingAddress().getCompleteAddress(shippingAddrBlock.getShippingAddress()));
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
        valid &= validateRequired(expirationMonthVw);
        valid &= validateRequired(expirationYearVw);
        CreditCard.Type ccType = CreditCard.Type.detect(cardNumberVw.getText().toString().replaceAll(" ", ""));
        if (ccType.isCidUsed()) {
            valid &= validateRequired(cidVw);
        }
        if (valid) {
            PaymentMethod paymentMethod = new PaymentMethod();
            paymentMethod.setSaveCardIndicator("Y");
            paymentMethod.setCardNumber(cardNumberVw.getText().toString().replaceAll(" ", ""));
            paymentMethod.setCardType(ccType.getName());
            paymentMethod.setCardExpirationMonth(expirationMonthVw.getText().toString());
            paymentMethod.setCardExpirationYear("20" +expirationYearVw.getText().toString());
            if (ccType.isCidUsed()) {
                paymentMethod.setCardVerificationCode(cidVw.getText().toString());
            }
            return paymentMethod;
        }
        return null;
    }

    private boolean readyForPrecheckout() {
        boolean billingAddrReady = true;
        boolean shippingAddrReady = shippingAddrBlock.validate();

        if(!useShipAddrAsBillingAddrSwitch.isChecked()) {
            billingAddrReady = billingAddrBlock.validateBillingAddress();
        }
        return (shippingAddrReady && billingAddrReady);
    }

    private void applyAddressesAndPrecheckout() {
        if(activity != null) activity.hideSoftKeyboard();
        if (!readyForPrecheckout()) {
            // reset shipping/tax info if already calculated and showing
            resetShippingAndTax();
            return;
        }
            showProgressIndicator();
        // add shipping address to cart if necessary, then billing address and precheckout
        if (shippingAddrNeedsApplying) {
            ShippingAddress shippingAddress = getShippingAddress();
            if (shippingAddress == null) {
                String errMsg = activity.getResources().getString(R.string.required_fields);
                Tracker.getInstance().trackActionForCheckoutFormErrors(errMsg); // analytics
                activity.showErrorDialog(errMsg);
                hideProgressIndicator();
            } else {
                if(shippingAddress.getCompleteAddress(shippingAddress) != null) {
                    shippingAddressTv.setText(shippingAddress.getCompleteAddress(shippingAddress));
                }
                CheckoutApiManager.applyShippingAddress(shippingAddress, new CheckoutApiManager.ApplyAddressCallback() {
                    @Override
                    public void onApplyAddressComplete(String addressId, String errMsg, String infoMsg) {

                        // if success
                        if (errMsg == null) {
                            shippingAddrNeedsApplying = false;

                            if (infoMsg != null) {
                                Tracker.getInstance().trackActionForCheckoutFormErrors("Shipping address alert: " + infoMsg); // analytics
                                // checking to see if the fragment is detached
                                if(getActivity() == null) return;
                                activity.showErrorDialog("Shipping address alert: " + infoMsg);
                            }

                            // now apply billing address
                            applyBillingAddressIfNeededAndPrecheckout();

                        } else {
                            // if shipping and tax already showing, need to hide them
                            resetShippingAndTax();
                            hideProgressIndicator();
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
                hideProgressIndicator();
            } else {
                if(billingAddress.getCompleteAddress(billingAddress) != null) {
                    billingAddressTv.setText(billingAddress.getCompleteAddress(billingAddress));
                }
                CheckoutApiManager.applyBillingAddress(billingAddress, new CheckoutApiManager.ApplyAddressCallback() {
                    @Override
                    public void onApplyAddressComplete(String addressId, String errMsg, String infoMsg) {

                        // if success
                        if (errMsg == null) {
                            billingAddrNeedsApplying = false;

                            if (infoMsg != null) {
                                Tracker.getInstance().trackActionForCheckoutFormErrors("Billing address alert: " + infoMsg); // analytics
                                // checking to see if the fragment is detached
                                if(getActivity() == null) return;
                                activity.showErrorDialog("Billing address alert: " + infoMsg);
                            }

                            // do precheckout
                            startPrecheckoutIfReady();

                        } else {
                            // if shipping and tax already showing, need to hide them
                            resetShippingAndTax();
                            hideProgressIndicator();
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
        // encrypt and apply payment method
        final PaymentMethod paymentMethod = getPaymentMethod();
        if(paymentMethod != null) {
            showProgressIndicator();
            CheckoutApiManager.encryptAndApplyPaymentMethod(paymentMethod, new CheckoutApiManager.ApplyPaymentMethodCallback() {
                @Override
                public void onApplyPaymentMethodComplete(String paymentMethodId, String authorized, String errMsg) {

                    // if success
                    if (errMsg == null) {
                        // submit the order
                        submitOrder(paymentMethod, shippingAddrBlock.getEmailAddress());

                    } else {
                        Tracker.getInstance().trackActionForCheckoutFormErrors(errMsg); // analytics
                        // checking to see if the fragment is detached
                        if(getActivity() == null) return;
                        activity.showErrorDialog(errMsg);
                        Log.d(TAG, errMsg);
                    }
                }
            });
        }
    }

    @Override
    protected void hideLayoutsInGuest(boolean isPrecheckOutComplete) {
        shippingAddrBlock.setVisibility(isPrecheckOutComplete?View.GONE:View.VISIBLE);
        preCheckOutValidateBtn.setVisibility(isPrecheckOutComplete?View.GONE:View.VISIBLE);
        guestPaymentLayoutVw.setVisibility(isPrecheckOutComplete?View.VISIBLE:View.GONE);
        preCheckoutShippingLayoutVw.setVisibility(isPrecheckOutComplete?View.VISIBLE:View.GONE);

        if(useShipAddrAsBillingAddrSwitch.isChecked()) {
            billingSwitchSelectLayoutVw.setVisibility(isPrecheckOutComplete?View.GONE:View.VISIBLE);
        } else {
            billingSwitchSelectLayoutVw.setVisibility(isPrecheckOutComplete?View.GONE:View.VISIBLE);
            billingAddrBlock.setVisibility(isPrecheckOutComplete?View.GONE:View.VISIBLE);
            billingAddressLbl.setVisibility(isPrecheckOutComplete?View.VISIBLE:View.GONE);
            preCheckoutBillingLayoutVw.setVisibility(isPrecheckOutComplete?View.VISIBLE:View.GONE);
        }
    }


    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.precheckout_validate_button:
                applyAddressesAndPrecheckout();
                break;
            case R.id.shipping_addon_layout:
                disableCheckoutButton(true);
                hideLayoutsInGuest(false);
                break;
            case R.id.billing_addon_layout:
                billingAddrBlock.requestFocus();
                disableCheckoutButton(true);
                hideLayoutsInGuest(false);
                break;
        }
    }
}
