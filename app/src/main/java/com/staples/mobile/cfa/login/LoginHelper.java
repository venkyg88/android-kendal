package com.staples.mobile.cfa.login;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;

import com.staples.mobile.cfa.MainActivity;
import com.staples.mobile.cfa.analytics.Tracker;
import com.staples.mobile.cfa.profile.ProfileDetails;
import com.staples.mobile.common.access.Access;
import com.staples.mobile.common.access.easyopen.api.EasyOpenApi;
import com.staples.mobile.common.access.easyopen.model.ApiError;
import com.staples.mobile.common.access.easyopen.model.login.CreateUserLogin;
import com.staples.mobile.common.access.easyopen.model.login.RegisteredUserLogin;
import com.staples.mobile.common.access.easyopen.model.login.TokenObject;
import com.staples.mobile.common.access.easyopen.model.member.Member;
import com.staples.mobile.common.access.easyopen.model.member.MemberDetail;
import com.staples.mobile.common.device.DeviceInfo;

import java.util.List;
import java.util.Vector;

import retrofit.Callback;
import retrofit.ResponseCallback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class LoginHelper {
    private static final String TAG = LoginHelper.class.getSimpleName();
    private static final String PREFS_USERNAME = "username";
    private static final String PREFS_ENCRYPTEDPASSWORD = "encryptedPassword";
    private static final String PREFS_TOKEN1 = "wcToken";
    private static final String PREFS_TOKEN2 = "wcTrustedToken";

    private static String cachedUsername;
    private static String cachedPassword;

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

    public void refreshSession() {
        if (isLoggedIn()) {
            if (isGuestLogin()) {
                getGuestTokens(true); // use refreshOnly=true
            } else {
                if (cachedUsername != null && cachedPassword != null) {
                    //set tokens to null first, otherwise api will think we're attempting to convert a guest login to a user login
                    setTokens(null, null, true);
                    getUserTokens(cachedUsername, cachedPassword, null, true); // use refreshOnly=true
                }
            }
        }
    }

    // load login info from cache and establish a session
    public void doCachedLogin(final ProfileDetails.ProfileRefreshCallback callback) {

        // load tokens and username/password from persisted cache
        boolean cacheAvail = loadCachedLoginInfo();
        final boolean registeredUser = isCachedUserRegistered();

        // if cached info available, see if it's valid
        if (cacheAvail) {
            // make call requiring secure api which will fail if expired tokens.
            // If the session is expired, this test will give us the desired
            // "_ERR_INVALID_COOKIE" failure regardless of guest or user session.
            // If the session is okay, it will fail for guest session (since no profile for guest)
            //  but with a different error code, so the following logic is okay.
            EasyOpenApi api = Access.getInstance().getEasyOpenApi(true);
            api.getMemberProfile(new Callback<MemberDetail>() {
                @Override
                public void success(MemberDetail memberDetail, Response response) {
                    // the tokens are good, so load profile and notify listeners
                    notifyListeners(!registeredUser, true);
                    loadProfile(callback);
                }

                @Override
                public void failure(RetrofitError error) {
                    ApiError apiError = ApiError.getApiError(error);
                    if (apiError.isAuthenticationError()) {
                        resetTokens(false);
                        ProfileDetails.resetMember();
                        if (registeredUser) {
                            getUserTokens(cachedUsername, cachedPassword, callback);
                        } else {
                            getGuestTokens();
                            if (callback != null) {
                                callback.onProfileRefresh(null, null);
                            }
                        }
                    } else {
                        // not an authentication error so the tokens are good, so load profile and notify listeners
                        // (e.g. will get "CMN5024E The current user is not registered." for a guest on this call)
                        notifyListeners(!registeredUser, true);
                        if (registeredUser) {
                            loadProfile(callback);
                        } else {
                            if (callback != null) {
                                callback.onProfileRefresh(null, null);
                            }
                        }
                    }
                }
            });
        } else {
            // since no cached login info, do a guest login
            getGuestTokens();
            if (callback != null) {
                callback.onProfileRefresh(null, null);
            }
        }
    }



    public void getGuestTokens() {
        getGuestTokens(false);
    }

    private void getGuestTokens(final boolean refreshOnly)
    {
        cachedUsername = null;
        cachedPassword = null;
        resetTokens(false);
        easyOpenApi.guestLogin(new Callback<TokenObject>() {

            @Override
            public void success(TokenObject tokenObjectReturned, Response response) {
                int code = response.getStatus();
                setTokens(tokenObjectReturned.getWCToken(), tokenObjectReturned.getWCTrustedToken(), true);
                if (!refreshOnly) {
                    notifyListeners(true, true); // guest login, signing in
                    Tracker.getInstance().setUserType(Tracker.UserType.GUEST); // update analytics header info
                }

                Log.i(TAG, "Status Code " + code);
                Log.i(TAG, "wcToken " + tokenObjectReturned.getWCToken());
                Log.i(TAG, "wctrustedToken " + tokenObjectReturned.getWCTrustedToken());
            }

            @Override
            public void failure(RetrofitError retrofitError) {
                String msg = ApiError.getErrorMessage(retrofitError);
                Log.d(TAG, msg);
            }
        });
    }

    private void loadProfile(final ProfileDetails.ProfileRefreshCallback callback) {
        new ProfileDetails().refreshProfile(new ProfileDetails.ProfileRefreshCallback() {
            @Override
            public void onProfileRefresh(Member member, String errMsg) {
                if (member == null) {
                    userSignOut();
                }

                if (callback != null) {
                    callback.onProfileRefresh(member, errMsg);
                }
            }
        });
    }

    //method to take entered username and password
    public void getUserTokens(String username, String password, final ProfileDetails.ProfileRefreshCallback callback) {
        getUserTokens(username, password, callback, false);
    }

    //method to take entered username and password
    private void getUserTokens(String username, String password, final ProfileDetails.ProfileRefreshCallback callback, final boolean refreshOnly)
    {
        cachedUsername = username;
        cachedPassword = password;
        RegisteredUserLogin user = new RegisteredUserLogin(username,password);
        easyOpenApi.registeredUserLogin(user, new Callback<TokenObject>() {

                    @Override
                    public void success(TokenObject tokenObjectReturned, Response response) {
                        int code = response.getStatus();
                        setTokens(tokenObjectReturned.getWCToken(), tokenObjectReturned.getWCTrustedToken(), false);
                        if (!refreshOnly) {
                            notifyListeners(false, true); // NOT guest login, signing in
                            Tracker.getInstance().setUserType(Tracker.UserType.REGISTERED); // update analytics header info
                            loadProfile(callback);
                        }
                        Log.i(TAG, "Status Code " + code);
                        Log.i(TAG, "wcToken " + tokenObjectReturned.getWCToken());
                        Log.i(TAG, "wctrustedToken " + tokenObjectReturned.getWCTrustedToken());
                    }

                    @Override
                    public void failure(RetrofitError retrofitError) {
                        Log.i(TAG, "Fail Message For Registered User " + retrofitError.getMessage());
                        Log.i(TAG, "Post URL address For Registered User " + retrofitError.getUrl());
                        // if no guest login yet, attempt a guest login
                        if (!isLoggedIn()) {
                            getGuestTokens(refreshOnly);
                        }
                        if (!refreshOnly) {
                            if (callback != null) {
                                callback.onProfileRefresh(null, ApiError.getErrorMessage(retrofitError));
                            }
                        }
                    }
                }
        );
    }

    public void registerUser(String emailAddress, String password, final ProfileDetails.ProfileRefreshCallback callback)
    {
        cachedUsername = emailAddress;
        cachedPassword = password;
        CreateUserLogin user = new CreateUserLogin(emailAddress, password);
        Log.i(TAG, "Register User object " + user);
        if (!isGuestLogin()) {
            resetTokens(false);
        }
        easyOpenApi.registerUser(user, new Callback<TokenObject>() {

                    @Override
                    public void success(TokenObject tokenObjectReturned, Response response) {
                        int code = response.getStatus();
                        setTokens(tokenObjectReturned.getWCToken(), tokenObjectReturned.getWCTrustedToken(), false);
                        notifyListeners(false, true); // NOT guest login, signing in
                        Tracker.getInstance().setUserType(Tracker.UserType.REGISTERED); // update analytics header info

                        loadProfile(callback);

                        Log.i(TAG, "Status Code " + code);
                        Log.i(TAG, "wcToken " + tokenObjectReturned.getWCToken());
                        Log.i(TAG, "wctrustedToken " + tokenObjectReturned.getWCTrustedToken());
                    }

                    @Override
                    public void failure(RetrofitError retrofitError) {
                        Log.i(TAG, "Fail Message to Register User: " + retrofitError.getMessage());
                        Log.i(TAG, "Post URL address For Register User: " + retrofitError.getUrl());
                        if (callback != null) {
                            callback.onProfileRefresh(null, ApiError.getErrorMessage(retrofitError));
                        }
                    }
                }
        );
    }

    public void userSignOut ()
    {
        easyOpenApi.registeredUserSignOut(new ResponseCallback() {
            @Override
            public void success(Response response) {
                Log.i(TAG, "Code for signout " + response.getStatus());
                handleSigningOut();
            }

            @Override
            public void failure(RetrofitError error) {
                Log.i(TAG, "Failed signout, URL: " + error.getUrl());
                // Even if sign out appears to fail, the app needs to act as though signed out because
                // there probably is not a legitimate session. For example, maybe signing out failed because
                // the last valid session had timed out.
                handleSigningOut();
            }

            private void handleSigningOut() {
                setTokens(null, null, true); //set these to null since they're definitely unusable now
                ProfileDetails.resetMember();
                notifyListeners(false, false); // signing OUT
                getGuestTokens(); // re-establish a guest login since user may try to add to cart after signing out
            }
        });
    }

    private void resetTokens(boolean persist) {
        setTokens(null, null, false, persist);
    }

    private void setTokens(String wcToken, String wcTrustedToken, boolean guestLogin) {
        setTokens(wcToken, wcTrustedToken, guestLogin, true);
    }

    private void setTokens(String wcToken, String wcTrustedToken, boolean guestLogin, boolean persist) {
        Access.getInstance().setTokens(wcToken, wcTrustedToken, guestLogin);
        if (persist) {
            persistLoginInfo(cachedUsername, cachedPassword, wcToken, wcTrustedToken);
        }
    }


    /** persists cached username and encrypted password */
    private void persistLoginInfo(String username, String password, String wcToken, String wcTrustedToken) {
        try {
            SharedPreferences prefs = activity.getPreferences(Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            if (!TextUtils.isEmpty(wcToken) && !TextUtils.isEmpty(wcTrustedToken)) {
                editor.putString(PREFS_TOKEN1, wcToken);
                editor.putString(PREFS_TOKEN2, wcTrustedToken);
                if (!TextUtils.isEmpty(username) && !TextUtils.isEmpty(password)) {
                    editor.putString(PREFS_USERNAME, username);
                    editor.putString(PREFS_ENCRYPTEDPASSWORD, AesCrypto.encrypt(password, getEncryptionKey()));
                } else {
                    editor.remove(PREFS_USERNAME);
                    editor.remove(PREFS_ENCRYPTEDPASSWORD);
                }
            } else {
                editor.remove(PREFS_TOKEN1);
                editor.remove(PREFS_TOKEN2);
                editor.remove(PREFS_USERNAME);
                editor.remove(PREFS_ENCRYPTEDPASSWORD);
            }
            editor.apply();
        } catch(Exception e) { /* eat any exceptions */ }
    }

    /**
     * loads cached username and encrypted password and tokens
     * @return true if successful
     */
    private boolean loadCachedLoginInfo() {
        String wcToken = null;
        String wcTrustedToken = null;
        try {
            SharedPreferences prefs = activity.getPreferences(Context.MODE_PRIVATE);
            cachedUsername = prefs.getString(PREFS_USERNAME, cachedUsername);
            String encryptedPassword = prefs.getString(PREFS_ENCRYPTEDPASSWORD, null);
            if (cachedUsername !=  null && encryptedPassword != null) {
                cachedUsername = cachedUsername.trim();
                cachedPassword = AesCrypto.decrypt(encryptedPassword.trim(), getEncryptionKey());
            }
            wcToken = prefs.getString(PREFS_TOKEN1, null);
            wcTrustedToken = prefs.getString(PREFS_TOKEN2, null);
        } catch(Exception e) { /* eat any exceptions */ }

        setTokens(wcToken, wcTrustedToken, !isCachedUserRegistered(), false);

        return (!TextUtils.isEmpty(wcToken) && !TextUtils.isEmpty(wcTrustedToken));
    }

    public boolean isCachedUserRegistered() {
        return (!TextUtils.isEmpty(cachedUsername) && !TextUtils.isEmpty(cachedPassword));
    }

    private String getEncryptionKey() {
        DeviceInfo deviceInfo = new DeviceInfo(activity.getResources());
        StringBuilder b = new StringBuilder();
        b.append(deviceInfo.getBrand())
            .append(deviceInfo.getDevice())
            .append(deviceInfo.getModel())
            .append(deviceInfo.getSerialNumber())
            .append(activity.getApplication().getPackageName());
        return b.toString();
    }
}
