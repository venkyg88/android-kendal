package com.staples.mobile.cfa.profile;

import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.staples.mobile.R;
import com.staples.mobile.cfa.MainActivity;
import com.staples.mobile.cfa.login.LoginHelper;
import com.staples.mobile.common.access.Access;
import com.staples.mobile.common.access.easyopen.api.EasyOpenApi;
import com.staples.mobile.common.access.easyopen.model.member.*;

import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class ProfileFragment extends Fragment implements ProfileDetails.ProfileRefreshCallback, View.OnClickListener{
    private static final String TAG = "ProfileFragment";

    private static final String RECOMMENDATION = "v1";
    private static final String STORE_ID = "10001";
    private static final String CLIENT_ID = LoginHelper.CLIENT_ID;
    private static final String LOCALE = "en_US";

    private EasyOpenApi easyOpenApi;
    Button shippingBtn;
    Button ccBtn;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {

        Log.d(TAG, "onCreateView()");
        View view = inflater.inflate(R.layout.profile_fragment, container, false);

        shippingBtn = (Button) view.findViewById(R.id.addShippingBtn);
        ccBtn = (Button) view.findViewById(R.id.addCCBtn);
        shippingBtn.setOnClickListener(this);
        ccBtn.setOnClickListener(this);

        new ProfileDetails().refreshProfile(this);
        return (view);
    }

    /** implements ProfileDetails.ProfileRefreshCallback */
    public void onProfileRefresh(Member member) {
        if (member==null) return;

        String email = member.getEmailAddress();
        String userName = member.getUserName();

        if (email!=null)
            ((TextView) getView().findViewById(R.id.emailProfile)).setText(email);
        if (userName!=null)
            ((TextView) getView().findViewById(R.id.userNameProfile)).setText(userName);

        List<Address> addresses = member.getAddress();
        if (addresses!=null) {
            int addressCount = addresses.size();
            Address address = addresses.get(0);
            if (address != null) {
                String tmpAddress = address.getAddress1() + " " + address.getCity() + " " + address.getState() + " " + address.getZipcode();
                ((TextView) getView().findViewById(R.id.addressET)).setText(tmpAddress);
                if(addressCount > 1) {
                    shippingBtn.setText(addressCount-1 + " more");
                }
                else {
                    shippingBtn.setText("Add");
                }
            }
        }

        List<CCDetails> creditCards = member.getCreditCard();
        if(creditCards !=null) {
            int creditCardCount = creditCards.size();
            CCDetails creditCard = creditCards.get(0);
            if (creditCard != null) {
                String tmpCreditCard =  creditCard.getCardNumber() + " " + creditCard.getCardType();
                ((TextView) getView().findViewById(R.id.ccET)).setText(tmpCreditCard);
                if(creditCardCount > 1) {
                    ccBtn.setText(creditCardCount-1 + " more");
                }
                else {
                    ccBtn.setText("Add");
                }
            }
        }
    }


    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.addShippingBtn:
                Fragment shippingFragment = Fragment.instantiate(getActivity(), ShippingFragment.class.getName());
                ((MainActivity)getActivity()).navigateToFragment(shippingFragment);
                break;
            case R.id.addCCBtn:
                Fragment ccFragment = Fragment.instantiate(getActivity(), CreditCardFragment.class.getName());
                ((MainActivity)getActivity()).navigateToFragment(ccFragment);
                break;
        }
    }
}