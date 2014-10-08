package com.staples.mobile.cfa;

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

import com.staples.mobile.common.access.easyopen.api.EasyOpenApi;
import com.staples.mobile.common.access.easyopen.model.login.RegisteredUserLogin;
import com.staples.mobile.common.access.easyopen.model.login.TokenObject;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by Avinash Raja Dodda.
 */
public class LoginHelper {

    private static final String RECOMMENDATION = "v1";
    private static final String STORE_ID = "10001";
    private static final String CLIENT_ID = "N6CA89Ti14E6PAbGTr5xsCJ2IGaHzGwS";

    private Activity activity;
    private EasyOpenApi easyOpenApi;


    public LoginHelper(Activity activity) {
        this.activity = activity;
        MainApplication application = (MainApplication) activity.getApplication();
        easyOpenApi = application.getEasyOpenApi(true);
    }


    public void getRegisteredUserTokens()
    {
        RegisteredUserLogin user = new RegisteredUserLogin("testuser2","password");
        easyOpenApi.registeredUserLogin(user, RECOMMENDATION, STORE_ID, CLIENT_ID, new Callback<TokenObject>() {

                    @Override
                    public void success(TokenObject tokenObjectReturned, Response response) {
                        int code = response.getStatus();
                        MainApplication.setTokens(tokenObjectReturned.getWCToken(), tokenObjectReturned.getWCTrustedToken());
                        Toast.makeText(activity, tokenObjectReturned.getWCToken(), Toast.LENGTH_LONG).show();

                        Log.i("Status Code", " " + code);
                        Log.i("wcToken", tokenObjectReturned.getWCToken());
                        Log.i("wctrustedToken", tokenObjectReturned.getWCTrustedToken());
                    }

                    @Override
                    public void failure(RetrofitError retrofitError) {
                        Log.i("Fail Message For Registered User", " " + retrofitError.getMessage());
                        Log.i("Post URL address For Registered User", " " + retrofitError.getUrl());
                    }
                }
        );
    }

    public void getGuestTokens()
    {
        easyOpenApi.guestLogin(RECOMMENDATION, STORE_ID, CLIENT_ID, new Callback<TokenObject>() {

                    @Override
                    public void success(TokenObject tokenObjectReturned, Response response) {
                        int code = response.getStatus();
                        MainApplication.setTokens(tokenObjectReturned.getWCToken(), tokenObjectReturned.getWCTrustedToken());
                        Toast.makeText(activity, tokenObjectReturned.getWCToken(), Toast.LENGTH_LONG).show();

                        Log.i("Status Code", " " + code);
                        Log.i("wcToken", tokenObjectReturned.getWCToken());
                        Log.i("wctrustedToken", tokenObjectReturned.getWCTrustedToken());
                    }

                    @Override
                    public void failure(RetrofitError retrofitError) {
                        Log.i("Fail Message For Guest User", " " + retrofitError.getMessage());
                        Log.i("Post URL address For Guest User", " " + retrofitError.getUrl());
                    }
                }
        );
    }
}
