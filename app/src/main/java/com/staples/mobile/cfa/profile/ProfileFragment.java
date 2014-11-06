package com.staples.mobile.cfa.profile;

import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.staples.mobile.R;
import com.staples.mobile.common.access.Access;
import com.staples.mobile.common.access.easyopen.api.EasyOpenApi;
import com.staples.mobile.common.access.easyopen.model.member.*;

import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class ProfileFragment extends Fragment implements Callback<MemberDetail>{
    private static final String TAG = "ProfileFragment";

    private static final String RECOMMENDATION = "v1";
    private static final String STORE_ID = "10001";
    public static final String CLIENT_ID = "N6CA89Ti14E6PAbGTr5xsCJ2IGaHzGwS";
    //    public static final String CLIENT_ID = "JxP9wlnIfCSeGc9ifRAAGku7F4FSdErd"; // a client_id that works with prod
    private static final String LOCALE = "en_US";

    private EasyOpenApi easyOpenApi;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {

        Log.d(TAG, "onCreateView()");
        View view = inflater.inflate(R.layout.user_profile, container, false);

        easyOpenApi = Access.getInstance().getEasyOpenApi(true);
        easyOpenApi.getMemberProfile(RECOMMENDATION, STORE_ID, LOCALE, CLIENT_ID, this);
        easyOpenApi.getMemberAddress(RECOMMENDATION, STORE_ID, LOCALE, CLIENT_ID, this);
        easyOpenApi.getMemberCreditCardDetails(RECOMMENDATION, STORE_ID, LOCALE, CLIENT_ID, this);

        return (view);
    }

    @Override
    public void success(MemberDetail memberDetail, Response response) {
        int code = response.getStatus();
        Member member = memberDetail.getMember().get(0);
        MemberObject.setMember(member);
        if (member==null) return;

        String email = member.getEmailAddress();
        String userName = member.getUserName();

        if (email!=null)
            ((EditText) getView().findViewById(R.id.emailProfile)).setText(email);
        if (userName!=null)
            ((EditText) getView().findViewById(R.id.userNameProfile)).setText(userName);

        List<Address> addresses = member.getAddress();
        if (addresses!=null) {
            Address address = addresses.get(0);
            if (address != null) {
                String tmpAddress = address.getAddress1() + "\n" + address.getCity() + "\n" + address.getState() + "\n" + address.getZipcode();
                ((EditText) getView().findViewById(R.id.addressET)).setText(tmpAddress);
            }
        }

        List<CCDetails> creditCards = member.getCreditCard();
        if(creditCards !=null) {
            CCDetails creditCard = creditCards.get(0);
            if (creditCard != null) {
                String tmpCreditCard =  creditCard.getCardNumber() + "\n" + creditCard.getCardType();
                ((EditText) getView().findViewById(R.id.ccET)).setText(tmpCreditCard);
            }
        }
    }

    @Override
    public void failure(RetrofitError retrofitError) {
        Log.i("Fail message when getting member details", " " + retrofitError.getMessage());
        Log.i("URl used to get member details", " " + retrofitError.getUrl());
    }
}