/*
 * Copyright (c) 2014 Staples, Inc. All rights reserved.
 */

package com.staples.mobile.cfa.checkout;

import android.app.Dialog;
import android.app.Fragment;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.staples.mobile.cfa.MainActivity;
import com.staples.mobile.cfa.R;
import com.staples.mobile.cfa.login.LoginHelper;


public class ConfirmationFragment extends Fragment implements View.OnClickListener {
    public static final String TAG = ConfirmationFragment.class.getSimpleName();

    public static final String BUNDLE_PARAM_ORDERNUMBER = "orderNumber";
    public static final String BUNDLE_PARAM_EMAILADDR = "emailAddr";
    public static final String BUNDLE_PARAM_DELIVERY = "deliveryRange";
    public static final String BUNDLE_PARAM_TOTAL = "orderTotal";


    // saving around Activity object since getActivity() returns null after user navigates away from
    // fragment, but api call may still be returning
    private MainActivity activity;

    Dialog accountDialog;

    String emailAddress;

    /**
     * Create a new instance of ConfirmationFragment that will be initialized
     * with the given arguments.
     */
    public static ConfirmationFragment newInstance(String orderNumber, String emailAddress,
                                                   String deliveryRange, String total) {
        ConfirmationFragment f = new ConfirmationFragment();
        Bundle args = new Bundle();
        args.putString(ConfirmationFragment.BUNDLE_PARAM_ORDERNUMBER, orderNumber);
        args.putString(ConfirmationFragment.BUNDLE_PARAM_EMAILADDR, emailAddress);
        args.putString(ConfirmationFragment.BUNDLE_PARAM_DELIVERY, deliveryRange);
        args.putString(ConfirmationFragment.BUNDLE_PARAM_TOTAL, total);
        f.setArguments(args);
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        Log.d(TAG, "onCreateView()");

        activity = (MainActivity)getActivity();

        // inflate and get child views
        View view = inflater.inflate(R.layout.confirmation_fragment, container, false);
        TextView confirmationMsgVw = (TextView) view.findViewById(R.id.email_confirm_msg);
        TextView orderNumberVw = (TextView) view.findViewById(R.id.order_number);
        TextView deliveryRangeVw = (TextView) view.findViewById(R.id.delivery_range);
        TextView checkoutTotalVw = (TextView) view.findViewById(R.id.order_total);

        // Set click listeners
        view.findViewById(R.id.continue_shopping_btn).setOnClickListener((View.OnClickListener)activity);

        // get order info from bundle
        Bundle confirmationBundle = this.getArguments();
        String orderNumber = confirmationBundle.getString(BUNDLE_PARAM_ORDERNUMBER);
        emailAddress = confirmationBundle.getString(BUNDLE_PARAM_EMAILADDR);
        String deliveryRange = confirmationBundle.getString(BUNDLE_PARAM_DELIVERY);
        String total = confirmationBundle.getString(BUNDLE_PARAM_TOTAL);

        confirmationMsgVw.setText(String.format(getResources().getString(R.string.order_confirmation_msg3), emailAddress));
        orderNumberVw.setText(orderNumber);
        deliveryRangeVw.setText(deliveryRange);
        checkoutTotalVw.setText(total);

        // if guest login, allow user to create an account
        LoginHelper loginHelper = new LoginHelper(activity);
        if (loginHelper.isGuestLogin()) {
            view.findViewById(R.id.account_suggestion_layout).setVisibility(View.VISIBLE);
            view.findViewById(R.id.open_account_dlg_action).setOnClickListener(this);
        }

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
            case R.id.open_account_dlg_action:
                // open the account creation dialog
                accountDialog = new Dialog(activity);
                Window window = accountDialog.getWindow();
                window.requestFeature(Window.FEATURE_NO_TITLE);
                accountDialog.setContentView(R.layout.confirmation_create_account);
                ((EditText) accountDialog.findViewById(R.id.emailAddr)).setText(emailAddress);

                // set up button listeners
                accountDialog.findViewById(R.id.cancel).setOnClickListener(this);
                accountDialog.findViewById(R.id.create_account_button).setOnClickListener(this);
                accountDialog.findViewById(R.id.show_password).setOnClickListener(this);

                accountDialog.show();
//                window.setLayout(popupWidth, ViewGroup.LayoutParams.WRAP_CONTENT);
                break;
            case R.id.cancel:
                if (accountDialog != null) {
                    accountDialog.dismiss();
                    accountDialog = null;
                }
                break;
            case R.id.show_password:
                if (accountDialog != null) {
                    TextView showPasswordButton = (TextView)accountDialog.findViewById(R.id.show_password);
                    EditText passwordEditVw = (EditText)accountDialog.findViewById(R.id.password);
                    Resources r = getResources();
                    String hideText = r.getString(R.string.hide);
                    String showText = r.getString(R.string.show);
                    if (showPasswordButton.getText().toString().equals(hideText)) {
                        showPasswordButton.setText(showText);
                        passwordEditVw.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    } else {
                        showPasswordButton.setText(hideText);
                        passwordEditVw.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    }
                }
                break;
            case R.id.create_account_button:
                if (accountDialog != null) {
                    EditText passwordEditVw = (EditText) accountDialog.findViewById(R.id.password);
                    EditText emailAddressEditVw = (EditText) accountDialog.findViewById(R.id.emailAddr);
                    // TODO validate required fields
                    // TODO submit api call
                    Toast.makeText(activity, "TBD", Toast.LENGTH_SHORT).show();
                    accountDialog.dismiss();
                    accountDialog = null;
                }
                break;
        }
    }


}
