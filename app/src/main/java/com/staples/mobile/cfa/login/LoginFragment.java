package com.staples.mobile.cfa.login;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TabHost;
import android.widget.Toast;

import com.staples.mobile.R;
import com.staples.mobile.cfa.LoginHelper;

public class LoginFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = "LoginFragment";
    Button signInBtn;
    LoginHelper loginHelper;
    private String userName;
    private String password;

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

        return view;
    }

    @Override
    public void onClick(View v) {

        setUserName(((EditText)getView().findViewById(R.id.username)).getText().toString());
        setPassword(((EditText)getView().findViewById(R.id.password)).getText().toString());

        loginHelper = new LoginHelper(getActivity());
        if(getUserName() != null && getPassword() != null)
        {
            loginHelper.getUserTokens(getUserName(), getPassword());
        }
        else{
            Toast.makeText(getActivity(), "userName or Password cannot be null", Toast.LENGTH_LONG).show();
        }
    }
}
