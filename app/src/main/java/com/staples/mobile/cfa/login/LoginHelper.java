package com.staples.mobile.cfa.login;

import android.app.Activity;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.staples.mobile.cfa.R;
import com.staples.mobile.cfa.MainActivity;
import com.staples.mobile.cfa.profile.ProfileDetails;
import com.staples.mobile.common.access.Access;
import com.staples.mobile.common.access.easyopen.api.EasyOpenApi;
import com.staples.mobile.common.access.easyopen.model.ApiError;
import com.staples.mobile.common.access.easyopen.model.login.CreateUserLogin;
import com.staples.mobile.common.access.easyopen.model.login.RegisteredUserLogin;
import com.staples.mobile.common.access.easyopen.model.login.TokenObject;
import com.staples.mobile.common.access.easyopen.model.member.Member;

import java.util.List;
import java.util.Vector;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class LoginHelper {

    public interface OnLoginCompleteListener {
        public void onLoginComplete(boolean guestLevel);
    }

    private MainActivity activity;
    private EasyOpenApi easyOpenApi;

    // single static synchronized list of login complete listeners
    private static List<OnLoginCompleteListener> loginCompleteListeners = new Vector<OnLoginCompleteListener>();


    public LoginHelper(MainActivity activity) {
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
        easyOpenApi.guestLogin(new Callback<TokenObject>() {

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
                        Toast.makeText(activity, "Failed to Login As Guest: " + ApiError.getErrorMessage(retrofitError), Toast.LENGTH_LONG).show();
                        Log.i("Fail Message For Guest User", " " + retrofitError.getMessage());
                        Log.i("Post URL address For Guest User", " " + retrofitError.getUrl());
                    }
                }
        );
    }

    private void loadProfileAndOpenProfileFragment() {
        new ProfileDetails().refreshProfile(new ProfileDetails.ProfileRefreshCallback() {
            @Override public void onProfileRefresh(Member member) {
                activity.selectProfileFragment();
                activity.hideProgressIndicator();
            }
        });
    }

    //cloned method to take entered username and password..not to break if any one using the above method
    public void getUserTokens(String username, String password)
    {
        activity.showProgressIndicator();
        RegisteredUserLogin user = new RegisteredUserLogin(username,password);
        easyOpenApi.registeredUserLogin(user, new Callback<TokenObject>() {

                    @Override
                    public void success(TokenObject tokenObjectReturned, Response response) {
                        int code = response.getStatus();
                        Access.getInstance().setTokens(tokenObjectReturned.getWCToken(), tokenObjectReturned.getWCTrustedToken(), false);
                        notifyListeners(false);
                        Button signInText = (Button) activity.findViewById(R.id.account_button);
                        signInText.setText(R.string.signout_title);

                        loadProfileAndOpenProfileFragment();

                        Log.i("Status Code", " " + code);
                        Log.i("wcToken", tokenObjectReturned.getWCToken());
                        Log.i("wctrustedToken", tokenObjectReturned.getWCTrustedToken());
                    }

                    @Override
                    public void failure(RetrofitError retrofitError) {
                        activity.hideProgressIndicator();
                        Toast.makeText(activity, "Failed Login: " + ApiError.getErrorMessage(retrofitError), Toast.LENGTH_LONG).show();
                        Log.i("Fail Message For Registered User", " " + retrofitError.getMessage());
                        Log.i("Post URL address For Registered User", " " + retrofitError.getUrl());
                    }
                }
        );
    }

    public void registerUser(String emailAddress, String username, String password)
    {
        activity.showProgressIndicator();
        CreateUserLogin user = new CreateUserLogin(emailAddress, username, password);
        Log.i("Register User object", " " + user);
        easyOpenApi.registerUser(user, new Callback<TokenObject>() {

                    @Override
                    public void success(TokenObject tokenObjectReturned, Response response) {
                        int code = response.getStatus();
                        Access.getInstance().setTokens(tokenObjectReturned.getWCToken(), tokenObjectReturned.getWCTrustedToken(), false);
                        notifyListeners(false);
                        Button signInText = (Button) activity.findViewById(R.id.account_button);
                        signInText.setText(R.string.signout_title);

                        loadProfileAndOpenProfileFragment();

                        Log.i("Status Code", " " + code);
                        Log.i("wcToken", tokenObjectReturned.getWCToken());
                        Log.i("wctrustedToken", tokenObjectReturned.getWCTrustedToken());
                    }

                    @Override
                    public void failure(RetrofitError retrofitError) {
                        activity.hideProgressIndicator();
                        Toast.makeText(activity, "Failed to Register User" + "\n" + ApiError.getErrorMessage(retrofitError), Toast.LENGTH_LONG).show();
                        Log.i("Fail Message to Register User", " " + retrofitError.getMessage());
                        Log.i("Post URL address For Register User", " " + retrofitError.getUrl());
                    }
                }
        );
    }

    public void userSignOut ()
    {
        easyOpenApi.registeredUserSignOut(new Callback<Response>() {
            @Override
            public void success(Response empty, Response response) {
                Access.getInstance().setTokens(null, null, true); //set these to null since they're definitely unusable now
                getGuestTokens(); // re-establish a guest login since user may try to add to cart after signing out
                ProfileDetails.resetMember();
                Button signInText = (Button)activity.findViewById(R.id.account_button);
                signInText.setText(R.string.signin_title);
                Log.i("Code for signout", " " + response.getStatus());
            }

            @Override
            public void failure(RetrofitError error) {
                Log.i("Url for signout", " " + error.getUrl());
            }
        });
    }
}
