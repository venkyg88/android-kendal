package com.staples.mobile.cfa.login;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TabHost;
import android.widget.TextView;

import com.staples.mobile.cfa.MainActivity;
import com.staples.mobile.cfa.R;
import com.staples.mobile.cfa.profile.ProfileDetails;
import com.staples.mobile.cfa.widget.ActionBar;
import com.staples.mobile.common.access.Access;
import com.staples.mobile.common.access.easyopen.model.member.Member;

public class LoginFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = "LoginFragment";
    Button signInBtn;
    Button registerBtn;
    LoginHelper loginHelper;
    MainActivity activity;
    EditText registerEmailET;
    EditText registerPasswordET;
    EditText signInEmail;
    EditText signInPassword;
    TextView showPassword;
    TextView passwordTxt;
    TextView forgotPassword;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        Log.d(TAG, "onCreateView()");
        activity = (MainActivity)getActivity();
        loginHelper = new LoginHelper((MainActivity)getActivity());

        if(loginHelper.isLoggedIn() && !loginHelper.isGuestLogin()) {
            getFragmentManager().popBackStack();
            return null;
        }else {
            View view = inflater.inflate(R.layout.login_fragment, container, false);

            TabHost tabHost = (TabHost) view.findViewById(R.id.tabHost);
            tabHost.setup();

            TabHost.TabSpec tab1 = tabHost.newTabSpec("First Tab");
            TabHost.TabSpec tab2 = tabHost.newTabSpec("Second Tab");

            tab1.setIndicator("Sign In");
            tab1.setContent(R.id.tab1);

            tab2.setIndicator("Create Account");
            tab2.setContent(R.id.tab2);

            tabHost.addTab(tab1);
            tabHost.addTab(tab2);

            signInBtn = (Button) view.findViewById(R.id.submit_button);
            signInBtn.setOnClickListener(this);

            registerBtn = (Button) view.findViewById(R.id.register_button);
            registerBtn.setOnClickListener(this);

            registerEmailET = (EditText)view.findViewById(R.id.emailIdRegister);
            registerPasswordET = (EditText)view.findViewById(R.id.passwordRegister);
            signInEmail = (EditText) view.findViewById(R.id.username);
            signInPassword = (EditText)view.findViewById(R.id.password);
            passwordTxt = (TextView)view.findViewById(R.id.passwordTxt);
            showPassword = (TextView)view.findViewById(R.id.passwordLbl);
            showPassword.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String password = ((TextView)v).getText().toString();
                    if(password.equals("SHOW")){
                        ((TextView)v).setText(R.string.hide);
                        registerPasswordET.setTransformationMethod(null);
                        registerPasswordET.setSelection(registerPasswordET.getText().length());
                    }
                    else{
                        ((TextView)v).setText(R.string.show);
                        registerPasswordET.setTransformationMethod(new PasswordTransformationMethod());
                        registerPasswordET.setSelection(registerPasswordET.getText().length());
                    }
                }
            });

            forgotPassword = (TextView)view.findViewById(R.id.forgotPwdTV);
            forgotPassword.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Fragment passwordFragment = Fragment.instantiate(activity, ResetPasswordFragment.class.getName());
                    activity.navigateToFragment(passwordFragment);
                }
            });

            registerPasswordET.addTextChangedListener(new TextWatcher() {

                public void afterTextChanged(Editable s) {
                }

                public void beforeTextChanged(CharSequence s, int start,
                                              int count, int after) {
                }

                public void onTextChanged(CharSequence s, int start,
                                          int before, int count) {
                    if (s.length() != 0) {
                        showPassword.setVisibility(View.VISIBLE);
                        passwordTxt.setVisibility(View.VISIBLE);
                    }
                }
            });

            return view;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        ActionBar.getInstance().setConfig(ActionBar.Config.LOGIN);
    }


    @Override
    public void onClick(View view) {

        if(view == signInBtn){
            String email = signInEmail.getText().toString();
            String password = signInPassword.getText().toString();

            activity.hideSoftKeyboard(view);

            if(!email.isEmpty() && !password.isEmpty())
            {
                activity.showProgressIndicator();
                loginHelper.getUserTokens(email, password, new ProfileDetails.ProfileRefreshCallback() {
                    @Override
                    public void onProfileRefresh(Member member, String errMsg) {
                        activity.hideProgressIndicator();
                        if (member != null) {
                            activity.selectProfileFragment();
                        } else if (errMsg != null) {
                            activity.showErrorDialog(errMsg);
                        }
                    }
                });
            }
            else{
                activity.showErrorDialog(R.string.username_password_required);
            }
        }
        if(view == registerBtn){
            String email = registerEmailET.getText().toString();
            String password = registerPasswordET.getText().toString();

            activity.hideSoftKeyboard(view);

            if(!email.isEmpty() && !password.isEmpty())
            {
                activity.showProgressIndicator();
                loginHelper.registerUser(email, password, new ProfileDetails.ProfileRefreshCallback() {
                    @Override
                    public void onProfileRefresh(Member member, String errMsg) {
                        activity.hideProgressIndicator();
                        if (member != null) {
                            activity.selectProfileFragment();
                        } else if (errMsg != null) {
                            activity.showErrorDialog(errMsg);
                        }
                    }
                });
            }
            else{
                activity.showErrorDialog(R.string.username_password_required);
            }
        }
    }
}
