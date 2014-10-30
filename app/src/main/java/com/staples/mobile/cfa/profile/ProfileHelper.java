package com.staples.mobile.cfa.profile;

import android.app.Activity;
import android.util.Log;
import android.widget.EditText;

import com.staples.mobile.R;
import com.staples.mobile.common.access.Access;
import com.staples.mobile.common.access.easyopen.api.EasyOpenApi;
import com.staples.mobile.common.access.easyopen.model.member.Member;
import com.staples.mobile.common.access.easyopen.model.member.MemberDetail;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by Avinash Dodda.
 */
public class ProfileHelper {

    private static final String RECOMMENDATION = "v1";
    private static final String STORE_ID = "10001";
    public static final String CLIENT_ID = "N6CA89Ti14E6PAbGTr5xsCJ2IGaHzGwS";
    //    public static final String CLIENT_ID = "JxP9wlnIfCSeGc9ifRAAGku7F4FSdErd"; // a client_id that works with prod
    private static final String LOCALE = "en_US";

    private Activity activity;
    private EasyOpenApi easyOpenApi;
    Member member;

    public ProfileHelper(Activity activity) {
        this.activity = activity;
        easyOpenApi = Access.getInstance().getEasyOpenApi(true);
    }

    public Member getMemberData()
    {
        easyOpenApi.getMemberProfile(RECOMMENDATION, STORE_ID, LOCALE, CLIENT_ID, new Callback<MemberDetail>() {
                    @Override
                    public void success(MemberDetail memberDetail, Response response) {
                        int code = response.getStatus();
                        member = memberDetail.getMember().get(0);

                        Log.i("Member Name", member.getUserName());
                        Log.i("Member Email", member.getEmailAddress());
                        Log.i("Status Code", " " + code);
                    }

                    @Override
                    public void failure(RetrofitError retrofitError) {
                        Log.i("Fail message when getting member details", " " + retrofitError.getMessage());
                        Log.i("URl used to get member details", " " + retrofitError.getUrl());

                    }
                }
        );
        return member;
    }
}
