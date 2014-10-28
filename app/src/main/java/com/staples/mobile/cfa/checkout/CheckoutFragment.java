/*
 * Copyright (c) 2014 Staples, Inc. All rights reserved.
 */

package com.staples.mobile.cfa.checkout;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.staples.mobile.R;
import com.staples.mobile.cfa.MainActivity;
import com.staples.mobile.common.access.Access;
import com.staples.mobile.common.access.easyopen.api.EasyOpenApi;


public class CheckoutFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = CheckoutFragment.class.getSimpleName();

    private static final String RECOMMENDATION = "v1";
    private static final String STORE_ID = "10001";

    private static final String CATALOG_ID = "10051";
    private static final String LOCALE = "en_US";

    private static final String ZIPCODE = "01010";
    private static final String CLIENT_ID = "N6CA89Ti14E6PAbGTr5xsCJ2IGaHzGwS";

    private static final int MAXFETCH = 50;

    private TextView shippingAddrVw;
    private TextView paymentMethodVw;
    private TextView billingAddrVw;
    private TextView deliveryRangeVw;
    private TextView couponsRewardsVw;
    private TextView shippingVw;
    private TextView taxVw;
    private TextView checkoutTotalVw;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        Log.d(TAG, "onCreateView()");

        // inflate and get child views
        View view = inflater.inflate(R.layout.checkout_fragment, container, false);
        shippingAddrVw = (TextView) view.findViewById(R.id.checkout_shipping_addr);
        paymentMethodVw = (TextView) view.findViewById(R.id.checkout_payment_method);
        billingAddrVw = (TextView) view.findViewById(R.id.checkout_billing_addr);
        deliveryRangeVw = (TextView) view.findViewById(R.id.checkout_delivery_range);
        couponsRewardsVw = (TextView) view.findViewById(R.id.checkout_coupons_rewards);
        shippingVw = (TextView) view.findViewById(R.id.checkout_shipping);
        taxVw = (TextView) view.findViewById(R.id.checkout_tax);
        checkoutTotalVw = (TextView) view.findViewById(R.id.checkout_order_total);


        // todo: remove the following:
        shippingAddrVw.setText("Paul Gates\n56 Frost St #1\nCambridge, MA 02140");
        paymentMethodVw.setText("<card logo> Card ending in 3333");
        billingAddrVw.setText("Paul Gates\n56 Frost St #1\nCambridge, MA 02140");
        deliveryRangeVw.setText("Oct 25-29");
        shippingVw.setText("free");
        taxVw.setText("$0.99");
        checkoutTotalVw.setText("$99.99");

        // Set listeners
        view.findViewById(R.id.shipping_addr_add).setOnClickListener(this);
        view.findViewById(R.id.payment_method_add).setOnClickListener(this);
        view.findViewById(R.id.billing_addr_add).setOnClickListener(this);
        view.findViewById(R.id.checkout_submit).setOnClickListener(this);

        // get api object (need secure connection
        EasyOpenApi api = Access.getInstance().getEasyOpenApi(true);

        // call api for info
//        api.viewProfile(RECOMMENDATION, STORE_ID, CATALOG_ID, LOCALE,
//                                                              ZIPCODE, CLIENT_ID, null, MAXFETCH, this);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // hide topper
        if (getActivity() instanceof MainActivity) {
            MainActivity a = (MainActivity) getActivity();
            a.showTopper(false);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        // restore topper
        if (getActivity() instanceof MainActivity) {
            MainActivity a = (MainActivity) getActivity();
            a.showTopper(true);
        }

    }

    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.shipping_addr_add:
                Toast.makeText(getActivity(), "TBD", Toast.LENGTH_SHORT).show();
                break;
            case R.id.payment_method_add:
                Toast.makeText(getActivity(), "TBD", Toast.LENGTH_SHORT).show();
                break;
            case R.id.billing_addr_add:
                Toast.makeText(getActivity(), "TBD", Toast.LENGTH_SHORT).show();
                break;
            case R.id.checkout_submit:
                Toast.makeText(getActivity(), "TBD", Toast.LENGTH_SHORT).show();
                break;
        }
    }


    // Retrofit callbacks

//    @Override
//    public void success(SkuDetails sku, Response response) {
//    }
//
//    @Override
//    public void failure(RetrofitError retrofitError) {
//        Log.d(TAG, "Failure callback " + retrofitError);
//    }
}
