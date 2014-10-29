package com.staples.mobile.cfa.feed;

import android.app.Activity;
import android.util.Log;

import com.staples.mobile.cfa.LoginHelper;
import com.staples.mobile.common.access.Access;
import com.staples.mobile.common.access.easyopen.api.EasyOpenApi;
import com.staples.mobile.common.access.easyopen.model.member.Member;
import com.staples.mobile.common.access.easyopen.model.member.MemberDetail;

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
//    private static final String CLIENT_ID = "N6CA89Ti14E6PAbGTr5xsCJ2IGaHzGwS";
    private static final String CLIENT_ID = LoginHelper.CLIENT_ID;

    private Activity activity;

    private EasyOpenApi easyOpenApi;

    public FeedAdapter(Activity activity) {
        super();
        this.activity = activity;
        easyOpenApi = Access.getInstance().getEasyOpenApi(true);
    }

    public FeedAdapter()
    {
    }

    public void fill() {
        getMemberData();
    }

    public void getMemberData()
    {
        easyOpenApi.member(RECOMMENDATION, STORE_ID, LOCALE, CLIENT_ID, new Callback<MemberDetail>() {

                    @Override
                    public void success(MemberDetail memberDetail, Response response) {

                        int code = response.getStatus();
                        Member member = memberDetail.getMember().get(0);

                        Log.i("Member Name", member.getUserName());
                        Log.i("Member Email", member.getEmailAddress());
                        Log.i("Status Code", " " + code);
                    }

                    @Override
                    public void failure(RetrofitError retrofitError) {
                        Log.i("Fail message when getting member details", " " + retrofitError.getMessage());
                        Log.i("URl used to get member details", " "+retrofitError.getUrl());

                    }
                }
        );
    }
}