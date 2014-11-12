package com.staples.mobile.cfa.login;

import android.app.Activity;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.staples.mobile.R;
import com.staples.mobile.cfa.MainActivity;
import com.staples.mobile.cfa.profile.ProfileDetails;
import com.staples.mobile.common.access.Access;
import com.staples.mobile.common.access.easyopen.api.EasyOpenApi;
import com.staples.mobile.common.access.easyopen.model.login.CreateUserLogin;
import com.staples.mobile.common.access.easyopen.model.login.EmptyResponse;
import com.staples.mobile.common.access.easyopen.model.login.RegisteredUserLogin;
import com.staples.mobile.common.access.easyopen.model.login.TokenObject;

import java.util.List;
import java.util.Objects;
import java.util.Vector;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class LoginHelper {

    public interface OnLoginCompleteListener {
        public void onLoginComplete(boolean guestLevel);
    }

    private static final String RECOMMENDATION = "v1";
    private static final String STORE_ID = "10001";
//    public static final String CLIENT_ID = "N6CA89Ti14E6PAbGTr5xsCJ2IGaHzGwS";
    public static final String CLIENT_ID = "JxP9wlnIfCSeGc9ifRAAGku7F4FSdErd"; // a client_id that works in all env incl prod
    private static final String LOCALE = "en_US";

    private Activity activity;
    private EasyOpenApi easyOpenApi;
    Button signInText;


    // single static synchronized list of login complete listeners
    private static List<OnLoginCompleteListener> loginCompleteListeners = new Vector<OnLoginCompleteListener>();


    public LoginHelper(Activity activity) {
        this.activity = activity;
        easyOpenApi = Access.getInstance().getEasyOpenApi(true);
    }


    /** adds listener to be notified following successful login */
    public void registerLoginCompleteListener(OnLoginCompleteListener loginCompleteListener) {
        if (!loginCompleteListeners.contains(loginCompleteListener)) {
            loginCompleteListeners.add(loginCompleteListener);
        }
    }

    /** removes listener */
    public void unregisterLoginCompleteListener(OnLoginCompleteListener loginCompleteListener) {
        if (loginCompleteListeners.contains(loginCompleteListener)) {
            loginCompleteListeners.remove(loginCompleteListener);
        }
    }

    private void notifyListeners(boolean guestLogin) {
        if (loginCompleteListeners != null) {
            for (OnLoginCompleteListener listener : loginCompleteListeners) {
                listener.onLoginComplete(guestLogin);
            }
        }
    }

    /** returns true if logged in (i.e. if token exists) */
    public boolean isLoggedIn() {
        return Access.getInstance().isLoggedIn();
    }

    /** returns true if current login level is only guest */
    public boolean isGuestLogin() {
        return Access.getInstance().isGuestLogin();
    }


    public void getGuestTokens()
    {
        easyOpenApi.guestLogin(RECOMMENDATION, STORE_ID, CLIENT_ID, new Callback<TokenObject>() {

                    @Override
                    public void success(TokenObject tokenObjectReturned, Response response) {
                        int code = response.getStatus();
                        Access.getInstance().setTokens(tokenObjectReturned.getWCToken(), tokenObjectReturned.getWCTrustedToken(), true);
                        // Toast.makeText(activity, tokenObjectReturned.getWCToken(), Toast.LENGTH_SHORT).show();
                        notifyListeners(true);

                        Log.i("Status Code", " " + code);
                        Log.i("wcToken", tokenObjectReturned.getWCToken());
                        Log.i("wctrustedToken", tokenObjectReturned.getWCTrustedToken());
                    }

                    @Override
                    public void failure(RetrofitError retrofitError) {
                        Toast.makeText(activity, "Failed to Login As Guest", Toast.LENGTH_LONG).show();
                        Log.i("Fail Message For Guest User", " " + retrofitError.getMessage());
                        Log.i("Post URL address For Guest User", " " + retrofitError.getUrl());
                    }
                }
        );
    }

    //cloned method to take entered username and password..not to break if any one using the above method
    public void getUserTokens(String username, String password)
    {
        RegisteredUserLogin user = new RegisteredUserLogin(username,password);
        easyOpenApi.registeredUserLogin(user, RECOMMENDATION, STORE_ID, CLIENT_ID, new Callback<TokenObject>() {

                    @Override
                    public void success(TokenObject tokenObjectReturned, Response response) {
                        int code = response.getStatus();
                        Access.getInstance().setTokens(tokenObjectReturned.getWCToken(), tokenObjectReturned.getWCTrustedToken(), false);
                        notifyListeners(false);
                        ((MainActivity)activity).selectProfileFragment();
                        signInText = (Button)activity.findViewById(R.id.account_button);
                        signInText.setText("Sign Out");

                        Log.i("Status Code", " " + code);
                        Log.i("wcToken", tokenObjectReturned.getWCToken());
                        Log.i("wctrustedToken", tokenObjectReturned.getWCTrustedToken());
                    }

                    @Override
                    public void failure(RetrofitError retrofitError) {
                        Toast.makeText(activity, "Failed to Login As Registered User", Toast.LENGTH_LONG).show();
                        Log.i("Fail Message For Registered User", " " + retrofitError.getMessage());
                        Log.i("Post URL address For Registered User", " " + retrofitError.getUrl());
                    }
                }
        );
    }

    public void registerUser(String emailAddress, String username, String password)
    {
        CreateUserLogin user = new CreateUserLogin(emailAddress, username, password);
        Log.i("Register User object", " " + user);
        easyOpenApi.registerUser(user, RECOMMENDATION, STORE_ID, LOCALE, CLIENT_ID, new Callback<TokenObject>() {

                    @Override
                    public void success(TokenObject tokenObjectReturned, Response response) {
                        int code = response.getStatus();
                        Access.getInstance().setTokens(tokenObjectReturned.getWCToken(), tokenObjectReturned.getWCTrustedToken(), false);
                        notifyListeners(false);
                        ((MainActivity)activity).selectProfileFragment();
                        signInText = (Button)activity.findViewById(R.id.account_button);
                        signInText.setText("Sign Out");

                        Log.i("Status Code", " " + code);
                        Log.i("wcToken", tokenObjectReturned.getWCToken());
                        Log.i("wctrustedToken", tokenObjectReturned.getWCTrustedToken());
                    }

                    @Override
                    public void failure(RetrofitError retrofitError) {
                        Toast.makeText(activity, "Failed to Register User" + "\n" + retrofitError.getMessage(), Toast.LENGTH_LONG).show();
                        Log.i("Fail Message to Register User", " " + retrofitError.getMessage());
                        Log.i("Post URL address For Register User", " " + retrofitError.getUrl());
                    }
                }
        );
    }

    public void userSignOut ()
    {
        easyOpenApi.registeredUserSignOut(RECOMMENDATION, STORE_ID, CLIENT_ID, new Callback<Response>() {
            @Override
            public void success(Response empty, Response response) {
                Access.getInstance().setTokens(null, null, true); //set these to null since they're definitely unusable now
                getGuestTokens(); // re-establish a guest login since user may try to add to cart after signing out
                ProfileDetails.resetMember();
                Log.i("Code for signout", " " + response.getStatus());
            }

            @Override
            public void failure(RetrofitError error) {
                Log.i("Url for signout", " " + error.getUrl());
            }
        });
    }
}
