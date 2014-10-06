package com.staples.mobile.cfa.feed;

import android.app.Activity;
import android.util.Log;

import com.staples.mobile.cfa.MainApplication;
import com.staples.mobile.common.access.easyopen.api.EasyOpenApi;
import com.staples.mobile.common.access.feed.MemberDetail;
import com.staples.mobile.common.access.login.model.TokenObject;
import com.staples.mobile.common.access.login.model.UserLogin;

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

    private EasyOpenApi easyOpenApi;

    private TokenObject token;

    public FeedAdapter(Activity activity) {
        super();
        this.activity = activity;
        MainApplication application = (MainApplication) activity.getApplication();
        easyOpenApi = application.getEasyOpenApiSecure();
    }

    public FeedAdapter()
    {

    }

    public void fill() {
        easyOpenApi.member(RECOMMENDATION, STORE_ID, LOCALE, CLIENT_ID, new Callback<MemberDetail>() {

            @Override
            public void success(MemberDetail member, Response response) {

                int code = response.getStatus();
                String x = member.getUserName();
                String y = member.getEmailAddress();

                Log.i("Success Name", x);
                Log.i("Success Email", y);
                Log.i("Status Code", " " + code);
            }

            @Override
            public void failure(RetrofitError retrofitError) {
                Log.i("Fail Token", " " + retrofitError.getMessage());
                Log.i("Something More", " "+retrofitError.getUrl() + retrofitError.getResponse());

            }
        }
        );
    }

    public TokenObject getUserTokens()
    {
        UserLogin user = new UserLogin("testuser2","password");
        easyOpenApi.login(user, RECOMMENDATION, STORE_ID, CLIENT_ID, new Callback<TokenObject>() {

                    @Override
                    public void success(TokenObject tokenObject, Response response) {
                        token = tokenObject;
                        int code = response.getStatus();
                        String x = tokenObject.getWCToken();
                        String y = tokenObject.getWCTrustedToken();

                        Log.i("Success Token", x);
                        Log.i("Status Code", " " + code);
                    }

                    @Override
                    public void failure(RetrofitError retrofitError) {
                        Log.i("Fail Token", " " + retrofitError.getMessage());
                        Log.i("Something More", " "+retrofitError.getUrl() + retrofitError.getResponse());
                    }
                }
        );
       return token;
    }
}