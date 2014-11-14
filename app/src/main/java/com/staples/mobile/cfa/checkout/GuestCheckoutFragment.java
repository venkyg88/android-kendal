/*
 * Copyright (c) 2014 Staples, Inc. All rights reserved.
 */

package com.staples.mobile.cfa.checkout;

import android.content.res.Resources;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import com.staples.mobile.R;
import com.staples.mobile.cfa.login.LoginHelper;
import com.staples.mobile.common.access.Access;
import com.staples.mobile.common.access.easyopen.api.EasyOpenApi;
import com.staples.mobile.common.access.easyopen.model.ApiError;
import com.staples.mobile.common.access.easyopen.model.cart.BillingAddress;
import com.staples.mobile.common.access.easyopen.model.cart.PaymentMethod;
import com.staples.mobile.common.access.easyopen.model.cart.PaymentMethodResponse;
import com.staples.mobile.common.access.easyopen.model.cart.ShippingAddress;
import com.staples.mobile.common.access.easyopen.model.checkout.AddressValidationAlert;
import com.staples.mobile.common.access.easyopen.model.member.AddCreditCardPOW;
import com.staples.mobile.common.access.easyopen.model.member.POWResponse;

import java.util.ArrayList;
import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class GuestCheckoutFragment extends CheckoutFragment implements CompoundButton.OnCheckedChangeListener {
    private static final String TAG = GuestCheckoutFragment.class.getSimpleName();

    private static final String RECOMMENDATION = "v1";
    private static final String STORE_ID = "10001";

    private static final String CATALOG_ID = "10051";
    private static final String LOCALE = "en_US";

    private static final String CLIENT_ID = LoginHelper.CLIENT_ID;

    private static final int MAXFETCH = 50;

    private View guestEntryView;
    Switch useShipAddrAsBillingAddrSwitch;
    ViewGroup billingAddrContainer;
    View shippingAddrLayoutVw;
    View billingAddrLayoutVw;
    View paymentMethodLayoutVw;
    EditText emailAddrVw;
    EditText emailAddrReenterVw;
    Spinner spinner;

    private boolean shippingAddrNeedsApplying = true;
    private boolean billingAddrNeedsApplying = true;

    private boolean fakingIt;


    /** override this to specify layout for entry area */
    @Override
    protected int getEntryLayoutId() {
        return R.layout.checkout_guest_entry;
    }

    /** override this for variation on entry area */
    @Override
    protected void initEntryArea(View view) {

        guestEntryView = view;
        shippingAddrLayoutVw = view.findViewById(R.id.shipping_addr_layout);
        billingAddrLayoutVw = view.findViewById(R.id.billing_addr_layout);
        billingAddrContainer = (ViewGroup)view.findViewById(R.id.billing_addr_container);
        paymentMethodLayoutVw = view.findViewById(R.id.payment_method_layout);
        emailAddrVw = (EditText)guestEntryView.findViewById(R.id.emailAddr);
        emailAddrReenterVw = (EditText)guestEntryView.findViewById(R.id.emailAddrReenter);

        // set up cc type spinner
        spinner = (Spinner) view.findViewById(R.id.card_type_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(activity,
                R.array.cardtype_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        // if logged in as guest, show sign-in button
        LoginHelper loginHelper = new LoginHelper(activity);
        if (loginHelper.isLoggedIn() && loginHelper.isGuestLogin()) {
            view.findViewById(R.id.signin_button).setVisibility(View.VISIBLE);
        }

        // hide imported views' Save buttons
        shippingAddrLayoutVw.findViewById(R.id.addressSaveBtn).setVisibility(View.GONE);
        billingAddrLayoutVw.findViewById(R.id.addressSaveBtn).setVisibility(View.GONE);
        paymentMethodLayoutVw.findViewById(R.id.addCCBtn).setVisibility(View.GONE);


        // use temp button for now to fake address entry
        View temporaryButton = view.findViewById(R.id.temp_button);
        temporaryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shippingAddrNeedsApplying = true;
                billingAddrNeedsApplying = true;
                fakingIt = false;
                applyShippingAddress();
            }
        });
        View temporaryFakeButton = view.findViewById(R.id.temp_fake_button);
        temporaryFakeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shippingAddrNeedsApplying = true;
                billingAddrNeedsApplying = true;
                fakingIt = true;
                applyShippingAddress();
            }
        });

        // add listener to billing addr toggle button switch
        useShipAddrAsBillingAddrSwitch = (Switch)view.findViewById(R.id.useShipAddrAsBillingAddr_switch);
        useShipAddrAsBillingAddrSwitch.setChecked(true);
        useShipAddrAsBillingAddrSwitch.setOnCheckedChangeListener(this);

    }

    /** implements CompoundButton.OnCheckedChangeListener */
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//        Toast.makeText(activity, "Checked: " + isChecked, Toast.LENGTH_SHORT).show();
        billingAddrContainer.setVisibility(isChecked? View.GONE: View.VISIBLE);
    }

    private boolean validateRequiredField(EditText editText, String msg) {
        if (TextUtils.isEmpty(editText.getText())) {
            editText.setError(msg);
            return false;
        }
        return true;
    }

    /** gets shipping address from user's entries */
    private ShippingAddress getShippingAddress(View layoutView, boolean includeEmail) {
        Resources resources = getResources();
        boolean errors = false;

        EditText firstNameVw = (EditText)layoutView.findViewById(R.id.firstName);
        EditText lastNameVw = (EditText)layoutView.findViewById(R.id.lastName);
        EditText addressVw = (EditText)layoutView.findViewById(R.id.address);
        EditText cityVw = (EditText)layoutView.findViewById(R.id.city);
        EditText stateVw = (EditText)layoutView.findViewById(R.id.state);
        EditText phoneNumberVw = (EditText)layoutView.findViewById(R.id.phoneNumber);
        EditText zipCodeVw = (EditText)layoutView.findViewById(R.id.zipCode);

        // validate required fields
        String requiredMsg = resources.getString(R.string.required);
        if (!validateRequiredField(firstNameVw, requiredMsg)) { errors = true; }
        if (!validateRequiredField(firstNameVw, requiredMsg)) { errors = true; }
        if (!validateRequiredField(lastNameVw, requiredMsg)) { errors = true; }
        if (!validateRequiredField(addressVw, requiredMsg)) { errors = true; }
        if (!validateRequiredField(cityVw, requiredMsg)) { errors = true; }
        if (!validateRequiredField(stateVw, requiredMsg)) { errors = true; }
        if (!validateRequiredField(phoneNumberVw, requiredMsg)) { errors = true; }
        if (!validateRequiredField(zipCodeVw, requiredMsg)) { errors = true; }

        ShippingAddress shippingAddress = new ShippingAddress();
        shippingAddress.setDeliveryFirstName(firstNameVw.getText().toString());
        shippingAddress.setDeliveryLastName(lastNameVw.getText().toString());
        shippingAddress.setDeliveryAddress1(addressVw.getText().toString());
        shippingAddress.setDeliveryCity(cityVw.getText().toString());
        shippingAddress.setDeliveryState(stateVw.getText().toString());
        shippingAddress.setDeliveryPhone(phoneNumberVw.getText().toString());
        shippingAddress.setDeliveryZipCode(zipCodeVw.getText().toString());

        if (includeEmail) {
            shippingAddress.setEmailAddress(emailAddrVw.getText().toString());
            shippingAddress.setReenterEmailAddress(emailAddrReenterVw.getText().toString());
            if (!validateRequiredField(emailAddrVw, requiredMsg)) { errors = true; }
            if (!validateRequiredField(emailAddrReenterVw, requiredMsg)) { errors = true; }
            if (!shippingAddress.getEmailAddress().equals(shippingAddress.getReenterEmailAddress())) {
                String mismatchMsg = resources.getString(R.string.email_mismatch);
                emailAddrReenterVw.setError(mismatchMsg);
                Toast.makeText(activity, mismatchMsg, Toast.LENGTH_LONG);
                errors = true;
            }
        }
        return errors? null:shippingAddress;
    }


    /** gets shipping address from user's entries */
    private ShippingAddress getShippingAddress() {
        if (fakingIt) {
            return getFakeShippingAddress();
        }
        return getShippingAddress(shippingAddrLayoutVw, true);
    }

    /** gets billing address from user's entries */
    private BillingAddress getBillingAddress() {
        if (fakingIt) {
            return getFakeBillingAddress();
        }
        ShippingAddress shippingAddress = getShippingAddress(useShipAddrAsBillingAddrSwitch.isChecked()?
                shippingAddrLayoutVw : billingAddrLayoutVw, false);
        return (shippingAddress == null)? null : new BillingAddress(shippingAddress);
    }

    /** gets payment method from user's entries */
    private PaymentMethod getPaymentMethod() {
        if (fakingIt) {
            return getFakePaymentMethod();
        }

        Resources resources = getResources();
        boolean errors = false;

        EditText cardNumberVw = (EditText)paymentMethodLayoutVw.findViewById(R.id.cardNumber);
        EditText expirationMonthVw = (EditText)paymentMethodLayoutVw.findViewById(R.id.expirationMonth);
        EditText expirationYearVw = (EditText)paymentMethodLayoutVw.findViewById(R.id.expirationYear);
        EditText cidVw = (EditText)paymentMethodLayoutVw.findViewById(R.id.cid);

        // validate required fields
        String requiredMsg = resources.getString(R.string.required);
        if (!validateRequiredField((EditText)spinner.getSelectedView(), requiredMsg)) { errors = true; }
        if (!validateRequiredField(cardNumberVw, requiredMsg)) { errors = true; }
        if (!validateRequiredField(expirationMonthVw, requiredMsg)) { errors = true; }
        if (!validateRequiredField(expirationYearVw, requiredMsg)) { errors = true; }
        if (!validateRequiredField(cidVw, requiredMsg)) { errors = true; }

        if (!errors) {
            PaymentMethod paymentMethod = new PaymentMethod();
            paymentMethod.setCardType(spinner.getSelectedItem().toString());
            if (paymentMethod.getCardType().equals("MasterCard")) {
                paymentMethod.setCardType("Mastercard");
            } else if (paymentMethod.getCardType().startsWith("Discover")) {
                paymentMethod.setCardType("Discover");
            } else if (paymentMethod.getCardType().startsWith("Staples")) {
                paymentMethod.setCardType("Staples");
            }
            paymentMethod.setCardNumber(cardNumberVw.getText().toString());
            paymentMethod.setCardExpirationMonth(expirationMonthVw.getText().toString());
            paymentMethod.setCardExpirationYear(expirationYearVw.getText().toString());
            paymentMethod.setCardVerificationCode(cidVw.getText().toString());
            return paymentMethod;
        }
        return null;
    }

    /** gets fake shipping address */
    private ShippingAddress getFakeShippingAddress() {
        ShippingAddress shippingAddress = new ShippingAddress();
        shippingAddress.setDeliveryFirstName("Diana");
        shippingAddress.setDeliveryLastName("Sutlief");
        shippingAddress.setDeliveryAddress1("16041 27th Ave NE");
        shippingAddress.setDeliveryCity("Shoreline");
        shippingAddress.setDeliveryState("WA");
        shippingAddress.setDeliveryZipCode("98155");
        shippingAddress.setDeliveryPhone("206-362-8024");
        shippingAddress.setEmailAddress("diana.sutlief@staples.com");
        shippingAddress.setReenterEmailAddress("diana.sutlief@staples.com");
        return shippingAddress;
    }

    /** gets fake billing address */
    private BillingAddress getFakeBillingAddress() {
        return new BillingAddress(getFakeShippingAddress());
    }

    private PaymentMethod getFakePaymentMethod() {
        PaymentMethod fakePaymentMethod = new PaymentMethod();
        fakePaymentMethod.setCardType("Visa");
        fakePaymentMethod.setCardVerificationCode("123");
        fakePaymentMethod.setCardNumber("4111111111111111");
        fakePaymentMethod.setCardExpirationMonth("12");
        fakePaymentMethod.setCardExpirationYear("2020");
        return fakePaymentMethod;
    }

    
    private void applyShippingAddress() {
        ShippingAddress shippingAddress = getShippingAddress();

        // add shipping address to cart
        if (shippingAddress != null) {
            showProgressIndicator();
            secureApi.addShippingAddressToCart(shippingAddress, RECOMMENDATION, STORE_ID, LOCALE, CLIENT_ID,
                    new Callback<AddressValidationAlert>() {

                        @Override
                        public void success(AddressValidationAlert precheckoutResponse, Response response) {
                            String validationAlert = precheckoutResponse.getAddressValidationAlert();

                            if (validationAlert != null) {
                                Toast.makeText(activity, "Address alert: " + validationAlert, Toast.LENGTH_SHORT).show();
                            } else {
                                shippingAddrNeedsApplying = false;
                            }

                            if (!shippingAddrNeedsApplying && billingAddrNeedsApplying) {
                                applyBillingAddress();
                            } else {
                                hideProgressIndicator();
                                startPrecheckoutIfReady();
                            }
                        }

                        @Override
                        public void failure(RetrofitError retrofitError) {
                            String msg = "Address error: " + ApiError.getErrorMessage(retrofitError);
                            Log.d(TAG, msg);
                            Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show();
                            hideProgressIndicator();
                        }
                    });
        }
    }

    private void applyBillingAddress() {
        BillingAddress billingAddress = getBillingAddress();

        // add shipping address to cart
        if (billingAddress != null) {
            showProgressIndicator();
            secureApi.addBillingAddressToCart(billingAddress, RECOMMENDATION, STORE_ID, LOCALE, CLIENT_ID,
                    new Callback<AddressValidationAlert>() {

                        @Override
                        public void success(AddressValidationAlert precheckoutResponse, Response response) {
                            String validationAlert = precheckoutResponse.getAddressValidationAlert();

                            if (validationAlert != null) {
                                Toast.makeText(activity, "Address alert: " + validationAlert, Toast.LENGTH_SHORT).show();
                            } else {
                                billingAddrNeedsApplying = false;
                            }

                            startPrecheckoutIfReady();
                        }

                        @Override
                        public void failure(RetrofitError retrofitError) {
                            String msg = "Address error: " + ApiError.getErrorMessage(retrofitError);
                            Log.d(TAG, msg);
                            Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show();
                            hideProgressIndicator();
                        }
                    });
        } else {
            hideProgressIndicator();
        }
    }

    private void startPrecheckoutIfReady() {
        if (!shippingAddrNeedsApplying && !billingAddrNeedsApplying) {
            startPrecheckout();
        }
    }



    /** overriding to handle order submission */
    @Override
    protected void onSubmit() {
        submitPaymentMethod();
    }

    private void submitPaymentMethod() {

        PaymentMethod paymentMethod = getPaymentMethod();

        // first add selected payment method to cart
        if (paymentMethod != null) {
            showProgressIndicator();
            // encrypt payment method
            String powCardType = paymentMethod.getCardType().toUpperCase();
            AddCreditCardPOW creditCard = new AddCreditCardPOW(paymentMethod.getCardNumber(), powCardType);
            List<AddCreditCardPOW> ccList = new ArrayList<AddCreditCardPOW>();
            ccList.add(creditCard);

            // todo: find a better way to determine current environment
            if (EasyOpenApi.SECURE_ENDPOINT.equals("https://api.staples.com")) {
                EasyOpenApi powApi = Access.getInstance().getPOWApi();
                powApi.addCreditPOWCall(ccList, new PowListener(paymentMethod));
            } else {
                secureApi.addCreditPOWCallQA(ccList, RECOMMENDATION, CLIENT_ID, new PowListener(paymentMethod));
            }


//            powApi.addCreditPOWCall(ccList, new Callback<POWResponse[]>() {
//                @Override
//                public void success(POWResponse[] powList, Response response) {
//                    Log.i("packet", powList[0].getPacket());
//                    if ("0".equals(powList[0].getStatus()) && !TextUtils.isEmpty(powList[0].getPacket())) {
//                        paymentMethod.setCardNumber(powList[0].getPacket());
//
//                        // add payment method to cart
//                        secureApi.addPaymentMethodToCart(paymentMethod, RECOMMENDATION, STORE_ID, LOCALE, CLIENT_ID,
//                                new Callback<PaymentMethodResponse>() {
//                                    @Override
//                                    public void success(PaymentMethodResponse paymentMethodResponse, Response response) {
//                                        // upon payment method success, submit the order
//                                        submitOrder(paymentMethod.getCardVerificationCode());
//                                    }
//
//                                    @Override
//                                    public void failure(RetrofitError retrofitError) {
//                                        hideProgressIndicator();
//                                        Toast.makeText(activity, "Payment Error: " + ApiError.getErrorMessage(retrofitError), Toast.LENGTH_SHORT).show();
//                                    }
//                                }
//                        );
//                    } else {
//                        hideProgressIndicator();
//                        Toast.makeText(activity, "Payment Error", Toast.LENGTH_SHORT).show();
//                    }
//                }
//
//                @Override
//                public void failure(RetrofitError retrofitError) {
//                    hideProgressIndicator();
//                    Toast.makeText(activity, "Payment Error: " + ApiError.getErrorMessage(retrofitError), Toast.LENGTH_SHORT).show();
//                }
//            });

        } else {
            Toast.makeText(activity, R.string.payment_method_required, Toast.LENGTH_SHORT).show();
        }
    }

    class PowListener implements Callback<List<POWResponse>> {

        PaymentMethod paymentMethod;

        PowListener(PaymentMethod paymentMethod) {
            this.paymentMethod = paymentMethod;
        }

        @Override
        public void success(List<POWResponse> powList, Response response) {
            Log.i("packet", powList.get(0).getPacket());
            if ("0".equals(powList.get(0).getStatus()) && !TextUtils.isEmpty(powList.get(0).getPacket())) {
                paymentMethod.setCardNumber(powList.get(0).getPacket());

                // add payment method to cart
                secureApi.addPaymentMethodToCart(paymentMethod, RECOMMENDATION, STORE_ID, LOCALE, CLIENT_ID,
                        new retrofit.Callback<PaymentMethodResponse>() {
                            @Override
                            public void success(PaymentMethodResponse paymentMethodResponse, Response response) {
                                // upon payment method success, submit the order
                                submitOrder(paymentMethod.getCardVerificationCode());
                            }

                            @Override
                            public void failure(RetrofitError retrofitError) {
                                hideProgressIndicator();
                                Toast.makeText(activity, "Payment Error: " + ApiError.getErrorMessage(retrofitError), Toast.LENGTH_SHORT).show();
                            }
                        }
                );
            } else {
                hideProgressIndicator();
                Toast.makeText(activity, "Payment Error", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void failure(RetrofitError retrofitError) {
            hideProgressIndicator();
            Toast.makeText(activity, "Payment Error: " + ApiError.getErrorMessage(retrofitError), Toast.LENGTH_SHORT).show();
        }
    }

}
