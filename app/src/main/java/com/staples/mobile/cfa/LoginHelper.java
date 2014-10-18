package com.staples.mobile.cfa;

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

import com.staples.mobile.common.access.Access;
import com.staples.mobile.common.access.easyopen.api.EasyOpenApi;
import com.staples.mobile.common.access.easyopen.model.login.RegisteredUserLogin;
import com.staples.mobile.common.access.easyopen.model.login.TokenObject;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class LoginHelper {

    private static final String RECOMMENDATION = "v1";
    private static final String STORE_ID = "10001";
    private static final String CLIENT_ID = "N6CA89Ti14E6PAbGTr5xsCJ2IGaHzGwS";

    private Activity activity;
    private EasyOpenApi easyOpenApi;

    public LoginHelper(Activity activity) {
        this.activity = activity;
        easyOpenApi = Access.getInstance().getEasyOpenApi(true);
    }


//    public void getRegisteredUserTokens()
//    {
//        RegisteredUserLogin user = new RegisteredUserLogin("testuser2","password");
//
//        easyOpenApi.registeredUserLogin(user, RECOMMENDATION, STORE_ID, CLIENT_ID, new Callback<TokenObject>() {
//
//                    @Override
//                    public void success(TokenObject tokenObjectReturned, Response response) {
//                        int code = response.getStatus();
//                        Access.getInstance().setTokens(tokenObjectReturned.getWCToken(), tokenObjectReturned.getWCTrustedToken(), false);
////                        Toast.makeText(activity, tokenObjectReturned.getWCToken(), Toast.LENGTH_LONG).show();
//
//                        Log.i("Status Code", " " + code);
//                        Log.i("wcToken", tokenObjectReturned.getWCToken());
//                        Log.i("wctrustedToken", tokenObjectReturned.getWCTrustedToken());
//                    }
//
//                    @Override
//                    public void failure(RetrofitError retrofitError) {
//
////                        Toast.makeText(activity, "Failed to Login As Registered User", Toast.LENGTH_LONG).show();
//
//                        Log.i("Fail Message For Registered User", " " + retrofitError.getMessage());
//                        Log.i("Post URL address For Registered User", " " + retrofitError.getUrl());
//                    }
//                }
//        );
//    }

    /** adds listener to be notified following successful login */
    public void registerLoginCompleteListener(Access.OnLoginCompleteListener loginCompleteListener) {
        Access.getInstance().registerLoginCompleteListener(loginCompleteListener);
    }

    /** removes listener */
    public void unregisterLoginCompleteListener(Access.OnLoginCompleteListener loginCompleteListener) {
        Access.getInstance().unregisterLoginCompleteListener(loginCompleteListener);
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
                        Toast.makeText(activity, tokenObjectReturned.getWCToken(), Toast.LENGTH_LONG).show();

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
}
