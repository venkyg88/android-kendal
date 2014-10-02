package com.staples.mobile.cfa.feed;

import android.app.Activity;
import android.util.Log;

import com.staples.mobile.cfa.MainApplication;
import com.staples.mobile.common.access.easyopen.api.EasyOpenApi;
import com.staples.mobile.common.access.login.TokenObject;
import com.staples.mobile.common.access.login.UserLogin;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by Avinash Dodda
 */
public class FeedAdapter {

    private static final String RECOMMENDATION = "v1";
    private static final String STORE_ID = "10001";

    private static final String CATALOG_ID = "10051";
    private static final String LOCALE = "en_US";

    private static final String ZIPCODE = "01010";
    private static final String CLIENT_ID = "N6CA89Ti14E6PAbGTr5xsCJ2IGaHzGwS";

    private Activity activity;

    public FeedAdapter(Activity activity) {
        super();
        this.activity = activity;
    }

    public void fill() {

        MainApplication application = (MainApplication) activity.getApplication();
        EasyOpenApi easyOpenApi = application.getEasyOpenApiSecure();
        UserLogin user = new UserLogin("sri","password");

        easyOpenApi.login(user, RECOMMENDATION, STORE_ID, CLIENT_ID, new Callback<TokenObject>() {

                    @Override
                    public void success(TokenObject tokenObject, Response response) {
                        int code = response.getStatus();
                        String x = tokenObject.getWCToken();
                        String y = tokenObject.getWCTrustedToken();

                        Log.i("successtoken", x);
                        Log.i("Status Code", " " + code);
                    }

                    @Override
                    public void failure(RetrofitError retrofitError) {
                        Log.i("failtoken", " " + retrofitError.getMessage());

                    }
                }
        );
    }
}