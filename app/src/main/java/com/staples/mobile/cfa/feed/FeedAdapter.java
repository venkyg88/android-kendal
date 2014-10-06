package com.staples.mobile.cfa.feed;

import android.app.Activity;
import android.util.Log;

import com.staples.mobile.cfa.MainApplication;
import com.staples.mobile.common.access.easyopen.api.EasyOpenApi;
import com.staples.mobile.common.access.feed.model.Member;
import com.staples.mobile.common.access.feed.model.MemberDetail;
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

    public FeedAdapter(Activity activity) {
        super();
        this.activity = activity;
        MainApplication application = (MainApplication) activity.getApplication();
        easyOpenApi = application.getEasyOpenApi(true);
    }

    public FeedAdapter()
    {

    }

    public void fill() {
        getUserTokens();
    }

    public void getUserTokens()
    {
        UserLogin user = new UserLogin("testuser2","password");
        easyOpenApi.login(user, RECOMMENDATION, STORE_ID, CLIENT_ID, new Callback<TokenObject>() {

                    @Override
                    public void success(TokenObject tokenObject, Response response) {
                        int code = response.getStatus();

                        MainApplication.setTokens(tokenObject.getWCToken(), tokenObject.getWCTrustedToken());
                        Log.i("Status Code", " " + code);
                        Log.i("wc", tokenObject.getWCToken());
                        Log.i("wctrusted", tokenObject.getWCTrustedToken());
                        getMemberData();
                    }

                    @Override
                    public void failure(RetrofitError retrofitError) {
                        Log.i("Fail Token", " " + retrofitError.getMessage());
                        Log.i("Something More", " "+retrofitError.getUrl() + retrofitError.getResponse());
                    }
                }
        );
    }

    public void getMemberData()
    {
        easyOpenApi.member(RECOMMENDATION, STORE_ID, LOCALE, CLIENT_ID, new Callback<MemberDetail>() {

                    @Override
                    public void success(MemberDetail memberDetail, Response response) {

                        int code = response.getStatus();
                        Member member = memberDetail.getMember().get(0);


                        Log.i("Success Name", member.getUserName());
                        Log.i("Success Email", member.getEmailAddress());
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
}