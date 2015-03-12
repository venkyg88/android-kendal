/*
 * Copyright (c) 2014 Staples, Inc. All rights reserved.
 */

package com.staples.mobile.cfa.checkout;

import android.app.Dialog;
import android.app.Fragment;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.apptentive.android.sdk.Apptentive;

import com.staples.mobile.cfa.MainActivity;
import com.staples.mobile.cfa.R;
import com.staples.mobile.common.analytics.Tracker;
import com.staples.mobile.cfa.apptentive.ApptentiveSdk;
import com.staples.mobile.cfa.login.LoginHelper;
import com.staples.mobile.cfa.profile.ProfileDetails;
import com.staples.mobile.cfa.widget.ActionBar;
import com.staples.mobile.cfa.widget.RelativeLayoutWithProgressOverlay;
import com.staples.mobile.common.access.easyopen.model.member.Member;

public class ConfirmationFragment extends Fragment implements View.OnClickListener {
    public static final String TAG = ConfirmationFragment.class.getSimpleName();

    public static final String BUNDLE_PARAM_ORDERNUMBER = "orderNumber";
    public static final String BUNDLE_PARAM_EMAILADDR = "emailAddr";
    public static final String BUNDLE_PARAM_DELIVERY = "deliveryRange";
    public static final String BUNDLE_PARAM_TOTAL = "orderTotal";

    // saving around Activity object since getActivity() returns null after user navigates away from
    // fragment, but api call may still be returning
    private MainActivity activity;

    View accountSuggestionLayout;
    View accountConfirmationLayout;

    Dialog accountDialog;
    RelativeLayoutWithProgressOverlay accountDialogLayout;

    String emailAddress;
    String orderNumber;


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
        activity = (MainActivity) getActivity();

        // inflate and get child views
        View view = inflater.inflate(R.layout.confirmation_fragment, container, false);
        TextView confirmationMsgVw = (TextView) view.findViewById(R.id.email_confirm_msg);
        TextView orderNumberVw = (TextView) view.findViewById(R.id.order_number);
        TextView deliveryRangeVw = (TextView) view.findViewById(R.id.delivery_range);
        TextView checkoutTotalVw = (TextView) view.findViewById(R.id.order_total);
        accountSuggestionLayout = view.findViewById(R.id.account_suggestion_layout);
        accountConfirmationLayout = view.findViewById(R.id.account_confirmation_layout);

        // Set click listeners
        view.findViewById(R.id.continue_shopping_btn).setOnClickListener((View.OnClickListener) activity);

        // get order info from bundle
        Bundle confirmationBundle = this.getArguments();
        orderNumber = confirmationBundle.getString(BUNDLE_PARAM_ORDERNUMBER);
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
            accountSuggestionLayout.setVisibility(View.VISIBLE);
            view.findViewById(R.id.open_account_dlg_action).setOnClickListener(this);
        }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        ActionBar.getInstance().setConfig(ActionBar.Config.CONFIRM);
        Tracker.getInstance().trackStateForOrderConfirmation(orderNumber);
        Apptentive.engage(activity, ApptentiveSdk.ORDER_CONFIRMATION_SHOWN_EVENT);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.open_account_dlg_action:
                // open the account creation dialog
                accountDialog = new Dialog(activity);
                Window window = accountDialog.getWindow();
                window.requestFeature(Window.FEATURE_NO_TITLE);
                accountDialog.setContentView(R.layout.confirmation_create_account);
                accountDialogLayout = (RelativeLayoutWithProgressOverlay) accountDialog.findViewById(R.id.dialog_layout);
                accountDialogLayout.setProgressOverlay(accountDialog.findViewById(R.id.dialog_progress_overlay));

                ((EditText) accountDialog.findViewById(R.id.emailAddr)).setText(emailAddress);

                // set up button listeners
                accountDialog.findViewById(R.id.cancel).setOnClickListener(this);
                accountDialog.findViewById(R.id.create_account_button).setOnClickListener(this);
                accountDialog.findViewById(R.id.show_password).setOnClickListener(this);

                accountDialog.show();
                break;
            case R.id.show_password:
                TextView showPasswordButton = (TextView) accountDialog.findViewById(R.id.show_password);
                EditText passwordVw = (EditText) accountDialog.findViewById(R.id.password);
                Resources r = getResources();
                String hideText = r.getString(R.string.hide);
                String showText = r.getString(R.string.show);
                if (showPasswordButton.getText().toString().equals(hideText)) {
                    showPasswordButton.setText(showText);
                    passwordVw.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                } else {
                    showPasswordButton.setText(hideText);
                    passwordVw.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                }
                break;
            case R.id.cancel:
                activity.hideSoftKeyboard(view);
                accountDialog.dismiss();
                accountDialog = null;
                break;
            case R.id.create_account_button:
                activity.hideSoftKeyboard(view);
                String requiredMsg = getResources().getString(R.string.required);
                EditText emailAddressEditVw = (EditText) accountDialog.findViewById(R.id.emailAddr);
                EditText passwordEditVw = (EditText) accountDialog.findViewById(R.id.password);
                if (!validateRequiredField(emailAddressEditVw, requiredMsg)) {
                    break;
                }
                if (!validateRequiredField(passwordEditVw, requiredMsg)) {
                    break;
                }
                String emailAddress = emailAddressEditVw.getText().toString();
                String password = passwordEditVw.getText().toString();

                // submit api call
                accountDialogLayout.showProgressIndicator(true);
                new LoginHelper(activity).registerUser(emailAddress, password, new ProfileDetails.ProfileRefreshCallback() {
                    @Override
                    public void onProfileRefresh(Member member, String errMsg) {
                        accountDialogLayout.showProgressIndicator(false);
                        if (member != null) {
                            // after successful API call
                            if (accountDialog != null && accountDialog.isShowing()) {
                                accountDialog.dismiss();
                                accountDialog = null;
                                accountSuggestionLayout.setVisibility(View.GONE);
                                accountConfirmationLayout.setVisibility(View.VISIBLE);
                            }
                        } else if (errMsg != null) {
                            activity.showErrorDialog(errMsg);
                        }
                    }
                });
                break;
        }
    }

    private boolean validateRequiredField(TextView textView, String msg) {
        if (TextUtils.isEmpty(textView.getText())) {
            textView.setError(msg);
            return false;
        }
        return true;
    }
}
