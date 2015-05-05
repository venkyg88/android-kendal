package app.staples.mobile.cfa.login;

import android.app.Fragment;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TabHost;
import android.widget.TextView;

import com.crittercism.app.Crittercism;
import com.staples.mobile.common.access.easyopen.model.member.Member;
import com.staples.mobile.common.analytics.Tracker;

import app.staples.R;
import app.staples.mobile.cfa.DrawerItem;
import app.staples.mobile.cfa.MainActivity;
import app.staples.mobile.cfa.profile.ProfileDetails;
import app.staples.mobile.cfa.widget.ActionBar;

public class LoginFragment extends Fragment implements View.OnClickListener, ProfileDetails.ProfileRefreshCallback {
    private static final String TAG = LoginFragment.class.getSimpleName();
    private static final String TABID_SIGNIN = "Sign In Tab";
    private static final String TABID_REGISTER = "Register Tab";
    private static final String AUTH_ERROR = "_ERR_AUTHENTICATION_ERROR";

    public static final String BUNDLE_PARAM_RETURNTOCHECKOUT = "returnToCheckout";

    LoginHelper loginHelper;
    MainActivity activity;
    EditText registerEmailET;
    EditText registerPasswordET;
    EditText signInEmail;
    EditText signInPassword;
    TextView passwordTxt;
    TextView showPassword;

    boolean returnToCheckout;

    /**
     * Create a new instance of ConfirmationFragment that will be initialized
     * with the given arguments.
     */
    public static LoginFragment newInstance(boolean returnToCheckout) {
        LoginFragment f = new LoginFragment();
        Bundle args = new Bundle();
        args.putBoolean(LoginFragment.BUNDLE_PARAM_RETURNTOCHECKOUT, returnToCheckout);
        f.setArguments(args);
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        Crittercism.leaveBreadcrumb("LoginFragment:onCreateView(): Displaying the Login screen.");
        activity = (MainActivity)getActivity();
        loginHelper = new LoginHelper((MainActivity)getActivity());
        Resources r = activity.getResources();

        if(loginHelper.isLoggedIn() && !loginHelper.isGuestLogin()) {
            getFragmentManager().popBackStack();
            return null;
        }else {
            View view = inflater.inflate(R.layout.login_fragment, container, false);

            TabHost tabHost = (TabHost) view.findViewById(R.id.tabHost);
            tabHost.setup();

            TabHost.TabSpec tab1 = tabHost.newTabSpec(TABID_SIGNIN);
            TabHost.TabSpec tab2 = tabHost.newTabSpec(TABID_REGISTER);

            tab1.setIndicator(r.getString(R.string.login_title));
            tab1.setContent(R.id.login_signin);

            tab2.setIndicator(r.getString(R.string.create_account_title));
            tab2.setContent(R.id.login_create);

            tabHost.addTab(tab1);
            tabHost.addTab(tab2);

            tabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
                @Override
                public void onTabChanged(String tabId) {
                    if (tabId.equals(TABID_SIGNIN)) {
                        Tracker.getInstance().trackStateForLogin(); // Analytics
                    } else if (tabId.equals(TABID_REGISTER)) {
                        Tracker.getInstance().trackStateForRegister(); // Analytics
                    }
                }
            });

            Bundle checkoutBundle = this.getArguments();
            returnToCheckout = checkoutBundle.getBoolean(BUNDLE_PARAM_RETURNTOCHECKOUT);

            Button signInBtn = (Button) view.findViewById(R.id.submit_button);
            signInBtn.setOnClickListener(this);

            Button registerBtn = (Button) view.findViewById(R.id.register_button);
            registerBtn.setOnClickListener(this);

            registerEmailET = (EditText)view.findViewById(R.id.emailIdRegister);
            registerPasswordET = (EditText)view.findViewById(R.id.passwordRegister);
            signInEmail = (EditText) view.findViewById(R.id.username);
            signInPassword = (EditText)view.findViewById(R.id.password);
            passwordTxt = (TextView)view.findViewById(R.id.passwordTxt);

            showPassword = (TextView)view.findViewById(R.id.passwordLbl);
            showPassword.setOnClickListener(this);

            signInEmail.postDelayed(new Runnable() {
                @Override
                public void run() {
                    signInEmail.requestFocus();
                    activity.showSoftKeyboard(signInEmail);
                }
            }, 100);

            TextView forgotPassword = (TextView)view.findViewById(R.id.forgotPwdTV);
            forgotPassword.setOnClickListener(this);

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
        Tracker.getInstance().trackStateForLogin(); // Analytics
    }

    @Override
    public void onProfileRefresh(Member member, String errMsg) {
        MainActivity activity = (MainActivity) getActivity();
        if (activity==null) return;

        activity.hideProgressIndicator();
        if (member != null) {
            if (returnToCheckout) {
                activity.selectOrderCheckout();
            } else {
                activity.selectProfileFragment();
            }
        } else if (errMsg != null) {
            if(errMsg.contains(AUTH_ERROR)) {
                activity.showErrorDialog(R.string.login_error_msg);
            } else {
                activity.showErrorDialog(errMsg);
            }
        }
    }

    @Override
    public void onClick(View view) {
        String email, password;

        switch(view.getId()){
            case R.id.passwordLbl:
                password = ((TextView) view).getText().toString();
                if(password.equals("SHOW")){
                    ((TextView) view).setText(R.string.hide);
                    registerPasswordET.setTransformationMethod(null);
                    registerPasswordET.setSelection(registerPasswordET.getText().length());
                }
                else{
                    ((TextView) view).setText(R.string.show);
                    registerPasswordET.setTransformationMethod(new PasswordTransformationMethod());
                    registerPasswordET.setSelection(registerPasswordET.getText().length());
                }
                break;

            case R.id.forgotPwdTV:
                Fragment passwordFragment = Fragment.instantiate(activity, ResetPasswordFragment.class.getName());
                activity.selectFragment(DrawerItem.PASSWORD, passwordFragment, MainActivity.Transition.RIGHT, true);
                break;

            case R.id.submit_button:
                email = signInEmail.getText().toString();
                password = signInPassword.getText().toString();

                activity.hideSoftKeyboard();

                if(!email.isEmpty() && !password.isEmpty())
                {
                    activity.showProgressIndicator();
                    loginHelper.getUserTokens(email, password, this);
                } else {
                    activity.showErrorDialog(R.string.username_password_required);
                }
                break;

            case R.id.register_button:
                email = registerEmailET.getText().toString();
                password = registerPasswordET.getText().toString();

                activity.hideSoftKeyboard();

                if(!email.isEmpty() && !password.isEmpty())
                {
                    activity.showProgressIndicator();
                    loginHelper.registerUser(email, password, this);
                } else {
                    activity.showErrorDialog(R.string.username_password_required);
                }
                break;
        }
    }
}
