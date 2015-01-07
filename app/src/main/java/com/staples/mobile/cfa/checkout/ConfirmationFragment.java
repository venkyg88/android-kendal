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

import com.staples.mobile.cfa.MainActivity;
import com.staples.mobile.cfa.R;
import com.staples.mobile.cfa.login.LoginHelper;
import com.staples.mobile.common.access.Access;
import com.staples.mobile.common.access.easyopen.api.EasyOpenApi;

import java.text.NumberFormat;

//import com.staples.mobile.common.access.easyopen.model.cart.OrderStatus;
//import com.staples.mobile.common.access.easyopen.model.cart.OrderStatusContents;


public class ConfirmationFragment extends Fragment implements View.OnClickListener {
    public static final String TAG = ConfirmationFragment.class.getSimpleName();




    private static final int MAXFETCH = 50;


    public static final String BUNDLE_PARAM_ORDERID = "orderId";
    public static final String BUNDLE_PARAM_ORDERNUMBER = "orderNumber";

    NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();

    // saving around Activity object since getActivity() returns null after user navigates away from
    // fragment, but api call may still be returning
    private MainActivity activity;

    private TextView orderNumberVw;

//    private TextView shippingAddrVw;
//    private TextView paymentMethodVw;
//    private TextView billingAddrVw;
//    private TextView deliveryRangeVw;
//    private TextView couponsRewardsVw;
//    private TextView shippingChargeVw;
//    private TextView taxVw;
//    private TextView checkoutTotalVw;
//    private EditText paymentCidVw;

    // api objects
    EasyOpenApi secureApi;

    // data returned from api



    // data initialized from cart drawer
    String orderId;
    String orderNumber;


    // api listeners
//    OrderStatusListener orderStatusListener;


    /**
     * Create a new instance of ConfirmationFragment that will be initialized
     * with the given arguments.
     */
    public static ConfirmationFragment newInstance(String orderId, String orderNumber) {
        ConfirmationFragment f = new ConfirmationFragment();
        Bundle args = new Bundle();
        if (orderNumber != null) {
            args.putString(ConfirmationFragment.BUNDLE_PARAM_ORDERID, orderId);
            args.putString(ConfirmationFragment.BUNDLE_PARAM_ORDERNUMBER, orderNumber);
        }
        f.setArguments(args);
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        Log.d(TAG, "onCreateView()");

        activity = (MainActivity)getActivity();

        // inflate and get child views
        View view = inflater.inflate(R.layout.confirmation_fragment, container, false);
        orderNumberVw = (TextView) view.findViewById(R.id.order_number);
//        shippingAddrVw = (TextView) view.findViewById(R.id.checkout_shipping_addr);
//        paymentMethodVw = (TextView) view.findViewById(R.id.checkout_payment_method);
//        billingAddrVw = (TextView) view.findViewById(R.id.checkout_billing_addr);
//        deliveryRangeVw = (TextView) view.findViewById(R.id.checkout_delivery_range);
//        couponsRewardsVw = (TextView) view.findViewById(R.id.checkout_coupons_rewards);
//        shippingChargeVw = (TextView) view.findViewById(R.id.checkout_shipping);
//        taxVw = (TextView) view.findViewById(R.id.checkout_tax);
//        checkoutTotalVw = (TextView) view.findViewById(R.id.checkout_order_total);

        // Set click listeners
//        view.findViewById(R.id.continue_shopping_btn).setOnClickListener(this);
        view.findViewById(R.id.continue_shopping_btn).setOnClickListener((View.OnClickListener)activity);

        // get order info from bundle
        Bundle confirmationBundle = this.getArguments();
        orderId = confirmationBundle.getString(BUNDLE_PARAM_ORDERID);
        orderNumber = confirmationBundle.getString(BUNDLE_PARAM_ORDERNUMBER);
        orderNumberVw.setText("#"+orderNumber);


        // get api objects
//        api = Access.getInstance().getEasyOpenApi(false);
        secureApi = Access.getInstance().getEasyOpenApi(true);

        // create api listeners
//        orderStatusListener = new OrderStatusListener();


        // query for order status
//        showProgressIndicator();
//        secureApi.getOrderStatus(
//                 orderNumber, 1, 100, orderStatusListener);


        return view;
    }


    @Override
    public void onResume() {
        super.onResume();

        // update action bar
        activity.showActionBar(R.string.order_confirmation_title, 0, null);
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.continue_shopping_btn:

                break;
        }
    }

    private void showProgressIndicator() {
        activity.showProgressIndicator();
    }

    private void hideProgressIndicator() {
        activity.hideProgressIndicator();
    }


    // Retrofit callbacks

    /************* api listeners ************/

//    /** listens for completion of order status  */
//    class OrderStatusListener implements Callback<OrderStatusContents> {
//
//        @Override
//        public void success(OrderStatusContents orderStatusContents, Response response) {
//
//            Toast.makeText(activity, "Order status successfully retrieved", Toast.LENGTH_SHORT).show();
//
//            hideProgressIndicator();
//        }
//
//        @Override
//        public void failure(RetrofitError retrofitError) {
//            String msg = "Error retrieving order status: " + ApiError.getErrorMessage(retrofitError);
//            Log.d(TAG, msg);
//            Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show();
//            hideProgressIndicator();
//        }
//    }


}
