/*
 * Copyright (c) 2014 Staples, Inc. All rights reserved.
 */

package com.staples.mobile.cfa.checkout;

import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.staples.mobile.R;
import com.staples.mobile.cfa.login.LoginHelper;
import com.staples.mobile.common.access.Access;
import com.staples.mobile.common.access.easyopen.model.ApiError;
import com.staples.mobile.common.access.easyopen.model.cart.Address;
import com.staples.mobile.common.access.easyopen.model.cart.AddressDetail;
import com.staples.mobile.common.access.easyopen.model.cart.BillingAddress;
import com.staples.mobile.common.access.easyopen.model.cart.ShippingAddress;
import com.staples.mobile.common.access.easyopen.model.checkout.AddressValidationAlert;
import com.staples.mobile.common.access.easyopen.model.member.CCDetails;
import com.staples.mobile.common.access.easyopen.model.member.Member;
import com.staples.mobile.common.access.easyopen.model.member.MemberDetail;

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


    /** override this to specify layout for entry area */
    @Override
    protected int getEntryLayoutId() {
        return R.layout.checkout_guest_entry;
    }

    /** override this for variation on entry area */
    @Override
    protected void initEntryArea(View view) {

        guestEntryView = view;

        // hide imported view's Save button
        View shippingAddrLayoutVw = view.findViewById(R.id.shipping_addr_layout);
        shippingAddrLayoutVw.findViewById(R.id.addressSaveBtn).setVisibility(View.GONE);

        // add listener to billing addr toggle button switch
        Switch useShipAddrAsBillingAddrSwitch = (Switch)view.findViewById(R.id.useShipAddrAsBillingAddr_switch);
        useShipAddrAsBillingAddrSwitch.setChecked(true);
        useShipAddrAsBillingAddrSwitch.setOnCheckedChangeListener(this);

    }

    /** implements CompoundButton.OnCheckedChangeListener */
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        Toast.makeText(activity, "Checked: " + isChecked, Toast.LENGTH_SHORT).show();
    }

    /** overriding to handle order submission */
    @Override
    protected void onSubmit() {
        Toast.makeText(activity, "TBD", Toast.LENGTH_SHORT).show();
    }


    /************* api listeners ************/


//    /** listens for completion of cart address request */
//    class AddressDetailListener implements Callback<AddressDetail> {
//
//        boolean listeningForShippingAddr; // true if shipping address listener, false if billing address listener
//
//        AddressDetailListener(boolean listeningForShippingAddr) {
//            this.listeningForShippingAddr = listeningForShippingAddr;
//        }
//
//        @Override
//        public void success(AddressDetail addressDetail, Response response) {
//
//            Address address = null;
//            if (addressDetail != null && addressDetail.getAddress() != null &&
//                    addressDetail.getAddress().size() > 0) {
//                address = addressDetail.getAddress().get(0);
//            }
//
//            if (listeningForShippingAddr) {
//                shippingAddrResponseReceived = true;
//                GuestCheckoutFragment.this.shippingAddress = address;
//                shippingAddrVw.setText(formatAddress(address)); //"Paul Gates\n56 Frost St #1\nCambridge, MA 02140"
//            } else {
//                billingAddrResponseReceived = true;
//                GuestCheckoutFragment.this.billingAddress = address;
//                billingAddrVw.setText(formatAddress(address));
//            }
//            startSecondWaveIfReady();
//        }
//
//        @Override
//        public void failure(RetrofitError retrofitError) {
//
//            boolean normalAddrNotAvailResponse = (retrofitError.getResponse() != null &&
//                    retrofitError.getResponse().getStatus() == 400);
//
//
//            if (listeningForShippingAddr) {
//
//                // query for profile addresses
//                shippingAddrResponseReceived = true;
//            } else {
//                billingAddrResponseReceived = true;
//            }
//
//            if (!normalAddrNotAvailResponse) {
//                String msg = "Error getting " + (listeningForShippingAddr ? "shipping" : "billing") + " address: " + ApiError.getErrorMessage(retrofitError);
//                Log.d(TAG, msg);
//                Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show();
//            }
//
//            startSecondWaveIfReady();
//       }
//    }
//
//    /** listens for completion of precheckout */
//    class AddAddressListener implements Callback<AddressValidationAlert> {
//
//        boolean listeningForShippingAddr; // true if shipping address listener, false if billing address listener
//
//        AddAddressListener(boolean listeningForShippingAddr) {
//            this.listeningForShippingAddr = listeningForShippingAddr;
//        }
//
//        @Override
//        public void success(AddressValidationAlert precheckoutResponse, Response response) {
//            String validationAlert = precheckoutResponse.getAddressValidationAlert();
//
//            if (validationAlert != null) {
//                Toast.makeText(activity, "Address alert: " + validationAlert, Toast.LENGTH_SHORT).show();
//            }
//
//            if (listeningForShippingAddr) {
//                shippingAddrAddToCartResponseReceived = true;
//            } else {
//                billingAddrAddToCartResponseReceived = true;
//            }
//            startPrecheckoutIfReady();
//        }
//
//        @Override
//        public void failure(RetrofitError retrofitError) {
//
//            if (listeningForShippingAddr) {
//                shippingAddrAddToCartResponseReceived = true;
//            } else {
//                billingAddrAddToCartResponseReceived = true;
//            }
//
//            String msg = "Address error: " + ApiError.getErrorMessage(retrofitError);
//            Log.d(TAG, msg);
//            Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show();
//            hideProgressIndicator();
//        }
//    }

}
