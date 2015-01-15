package com.staples.mobile.cfa.login;

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
    private static final String TAG = "LoginHelper";

    public interface OnLoginCompleteListener {
        public void onLoginComplete(boolean guestLevel);
        public void onLogoutComplete();
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

    private void notifyListeners(boolean guestLogin, boolean signingIn) {
        if (loginCompleteListeners != null) {
            for (OnLoginCompleteListener listener : loginCompleteListeners) {
                if (signingIn) {
                    listener.onLoginComplete(guestLogin);
                } else {
                    listener.onLogoutComplete();
                }
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
                        notifyListeners(true, true); // guest login, signing in

                        Log.i("Status Code", " " + code);
                        Log.i("wcToken", tokenObjectReturned.getWCToken());
                        Log.i("wctrustedToken", tokenObjectReturned.getWCTrustedToken());
                    }

                    @Override
                    public void failure(RetrofitError retrofitError) {
                        String msg = ApiError.getErrorMessage(retrofitError);
                        Toast.makeText(activity, msg, Toast.LENGTH_LONG).show();
                        Log.d(TAG, msg);
                    }
                }
        );
    }

    private void loadProfile(final ProfileDetails.ProfileRefreshCallback callback) {
        new ProfileDetails().refreshProfile(new ProfileDetails.ProfileRefreshCallback() {
            @Override public void onProfileRefresh(Member member) {
                if(member == null) {
                    userSignOut();
                    Toast.makeText(activity, "Unable to load profile", Toast.LENGTH_SHORT).show();
                }

                if (callback != null) {
                    callback.onProfileRefresh(member);
                }
            }
        });
    }

    //cloned method to take entered username and password..not to break if any one using the above method
    public void getUserTokens(String username, String password, final ProfileDetails.ProfileRefreshCallback callback)
    {
        RegisteredUserLogin user = new RegisteredUserLogin(username,password);
        easyOpenApi.registeredUserLogin(user, new Callback<TokenObject>() {

                    @Override
                    public void success(TokenObject tokenObjectReturned, Response response) {
                        int code = response.getStatus();
                        Access.getInstance().setTokens(tokenObjectReturned.getWCToken(), tokenObjectReturned.getWCTrustedToken(), false);
                        notifyListeners(false, true); // NOT guest login, signing in

                        loadProfile(callback);

                        Log.i("Status Code", " " + code);
                        Log.i("wcToken", tokenObjectReturned.getWCToken());
                        Log.i("wctrustedToken", tokenObjectReturned.getWCTrustedToken());
                    }

                    @Override
                    public void failure(RetrofitError retrofitError) {
                        Toast.makeText(activity, "Failed Login: " + ApiError.getErrorMessage(retrofitError), Toast.LENGTH_LONG).show();
                        Log.i("Fail Message For Registered User", " " + retrofitError.getMessage());
                        Log.i("Post URL address For Registered User", " " + retrofitError.getUrl());
                        if (callback != null) {
                            callback.onProfileRefresh(null);
                        }
                    }
                }
        );
    }

    public void registerUser(String emailAddress, String username, String password, final ProfileDetails.ProfileRefreshCallback callback)
    {
        CreateUserLogin user = new CreateUserLogin(emailAddress, username, password);
        Log.i("Register User object", " " + user);
        easyOpenApi.registerUser(user, new Callback<TokenObject>() {

                    @Override
                    public void success(TokenObject tokenObjectReturned, Response response) {
                        int code = response.getStatus();
                        Access.getInstance().setTokens(tokenObjectReturned.getWCToken(), tokenObjectReturned.getWCTrustedToken(), false);
                        notifyListeners(false, true); // NOT guest login, signing in

                        loadProfile(callback);

                        Log.i("Status Code", " " + code);
                        Log.i("wcToken", tokenObjectReturned.getWCToken());
                        Log.i("wctrustedToken", tokenObjectReturned.getWCTrustedToken());
                    }

                    @Override
                    public void failure(RetrofitError retrofitError) {
                        Toast.makeText(activity, "Failed to Register User" + "\n" + ApiError.getErrorMessage(retrofitError), Toast.LENGTH_LONG).show();
                        Log.i("Fail Message to Register User", " " + retrofitError.getMessage());
                        Log.i("Post URL address For Register User", " " + retrofitError.getUrl());
                        if (callback != null) {
                            callback.onProfileRefresh(null);
                        }
                    }
                }
        );
    }

    public void userSignOut ()
    {
        easyOpenApi.registeredUserSignOut(new Callback<Response>() {
            @Override
            public void success(Response empty, Response response) {
                Log.i("Code for signout", " " + response.getStatus());
                handleSigningOut();
            }

            @Override
            public void failure(RetrofitError error) {
                Log.i("Failed signout, URL: ", " " + error.getUrl());
                // Even if sign out appears to fail, the app needs to act as though signed out because
                // there probably is not a legitimate session. For example, maybe signing out failed because
                // the last valid session had timed out.
                handleSigningOut();
            }

            private void handleSigningOut() {
                Access.getInstance().setTokens(null, null, true); //set these to null since they're definitely unusable now
                ProfileDetails.resetMember();
                notifyListeners(false, false); // signing OUT
                getGuestTokens(); // re-establish a guest login since user may try to add to cart after signing out
            }
        });
    }
}
