package com.staples.mobile.cfa.login;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TabHost;
import android.widget.Toast;

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
    private String userName;
    private String password;
    private String emaiId;
    private String registerUsername;
    private String registerPassword;
    MainActivity activity;

    public String getRegisterPassword() {
        return registerPassword;
    }

    public void setRegisterPassword(String registerPassword) {
        this.registerPassword = registerPassword;
    }

    public String getRegisterUsername() {
        return registerUsername;
    }

    public void setRegisterUsername(String registerUsername) {
        this.registerUsername = registerUsername;
    }

    public String getEmaiId() {
        return emaiId;
    }

    public void setEmaiId(String emaiId) {
        this.emaiId = emaiId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        Log.d(TAG, "onCreateView()");
        activity = (MainActivity)getActivity();
        loginHelper = new LoginHelper((MainActivity)getActivity());
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

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        ActionBar.getInstance().setConfig(ActionBar.Config.LOGIN);
    }

    public void hideKeyboard(View view)
    {
        InputMethodManager keyboard = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        keyboard.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    @Override
    public void onClick(View view) {

        if(view == signInBtn)
        {
            String userName = ((EditText) getView().findViewById(R.id.username)).getText().toString();
            String password = ((EditText)getView().findViewById(R.id.password)).getText().toString();

            setUserName(userName);
            setPassword(password);

            hideKeyboard(view);

            if(!getUserName().isEmpty() && !getPassword().isEmpty())
            {
                activity.showProgressIndicator();
                loginHelper.getUserTokens(getUserName(), getPassword(), new ProfileDetails.ProfileRefreshCallback() {
                    @Override
                    public void onProfileRefresh(Member member) {
                        activity.hideProgressIndicator();
                        if (member != null) {
                            activity.selectProfileFragment();
                        }
                    }
                });
            }
            else{
                Toast.makeText(getActivity(), "Username or Password cannot be null", Toast.LENGTH_LONG).show();
            }
        }
        if(view == registerBtn)
        {
            setEmaiId(((EditText) getView().findViewById(R.id.emailIdRegister)).getText().toString());
            setRegisterUsername(((EditText) getView().findViewById(R.id.userNameRegister)).getText().toString());
            setRegisterPassword(((EditText) getView().findViewById(R.id.passwordRegister)).getText().toString());

            hideKeyboard(view);

            if(!getRegisterUsername().isEmpty() && !getRegisterPassword().isEmpty())
            {
                if (!loginHelper.isGuestLogin()) {
                    Access.getInstance().setTokens(null, null, false);
                }
                activity.showProgressIndicator();
                loginHelper.registerUser(getEmaiId(), getRegisterUsername(), getRegisterPassword(), new ProfileDetails.ProfileRefreshCallback() {
                    @Override
                    public void onProfileRefresh(Member member) {
                        activity.hideProgressIndicator();
                        if (member != null) {
                            activity.selectProfileFragment();
                        }
                    }
                });
            }
            else{
                Toast.makeText(getActivity(), "Username or Password cannot be null", Toast.LENGTH_LONG).show();
            }
        }
    }
}
