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

import com.staples.mobile.R;
import com.staples.mobile.common.access.Access;

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
        loginHelper = new LoginHelper(getActivity());
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
    public void onClick(View view) {

        if(view == signInBtn)
        {
            String userName = ((EditText) getView().findViewById(R.id.username)).getText().toString();
            String password = ((EditText)getView().findViewById(R.id.password)).getText().toString();

            setUserName(userName);
            setPassword(password);

            InputMethodManager keyboard = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            keyboard.hideSoftInputFromWindow(view.getWindowToken(), 0);

            if(!getUserName().isEmpty() && !getPassword().isEmpty())
            {
                loginHelper.getUserTokens(getUserName(), getPassword());
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

            if(!getRegisterUsername().isEmpty() && !getRegisterPassword().isEmpty())
            {
                Access.getInstance().setTokens(null, null, false);
                loginHelper.registerUser(getEmaiId(), getRegisterUsername(), getRegisterPassword());

            }
            else{
                Toast.makeText(getActivity(), "Username or Password cannot be null", Toast.LENGTH_LONG).show();
            }
        }

    }
}
