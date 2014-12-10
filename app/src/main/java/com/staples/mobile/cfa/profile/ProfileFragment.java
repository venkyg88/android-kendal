package com.staples.mobile.cfa.profile;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.staples.mobile.cfa.MainActivity;
import com.staples.mobile.cfa.R;
import com.staples.mobile.cfa.login.LoginHelper;
import com.staples.mobile.common.access.easyopen.api.EasyOpenApi;
import com.staples.mobile.common.access.easyopen.model.member.Address;
import com.staples.mobile.common.access.easyopen.model.member.CCDetails;
import com.staples.mobile.common.access.easyopen.model.member.Member;

import java.util.List;

public class ProfileFragment extends Fragment implements ProfileDetails.ProfileRefreshCallback, View.OnClickListener{
    private static final String TAG = "ProfileFragment";

    private static final String RECOMMENDATION = "v1";
    private static final String STORE_ID = "10001";
    private static final String CLIENT_ID = LoginHelper.CLIENT_ID;
    private static final String LOCALE = "en_US";

    private EasyOpenApi easyOpenApi;
    Button shippingBtn;
    Button ccBtn;
    MainActivity activity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {

        Log.d(TAG, "onCreateView()");
        activity = (MainActivity)getActivity();
        View view = inflater.inflate(R.layout.profile_fragment, container, false);

        shippingBtn = (Button) view.findViewById(R.id.addShippingBtn);
        ccBtn = (Button) view.findViewById(R.id.addCCBtn);
        shippingBtn.setOnClickListener(this);
        ccBtn.setOnClickListener(this);

        showProgressIndicator();
        new ProfileDetails().refreshProfile(this);

        return (view);
    }

    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity) activity).showActionBar(R.string.profile_title, 0, null);
    }

    /** implements ProfileDetails.ProfileRefreshCallback */
    public void onProfileRefresh(Member member) {
        hideProgressIndicator();
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
                String tmpAddress = address.getAddress1() + "\n" + address.getCity() + ", " + address.getState() + " " + address.getZipcode();
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
                String cardNumber;
                if (creditCard.getCardNumber().length() > 4) {
                    cardNumber = creditCard.getCardNumber().substring(creditCard.getCardNumber().length() - 4);
                } else {
                    cardNumber = creditCard.getCardNumber();
                }
                String tmpCreditCard =  cardNumber + " " + creditCard.getCardType();
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

    private void showProgressIndicator() {
        activity.showProgressIndicator();
    }

    private void hideProgressIndicator() {
        activity.hideProgressIndicator();
    }


    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.addShippingBtn:
                shippingBtn = (Button) view;
                if (shippingBtn.getText().equals("Add")) {
                    Fragment addressFragment = Fragment.instantiate(activity, AddressFragment.class.getName());
                    ((MainActivity) activity).navigateToFragment(addressFragment);
                    break;
                } else {
                    ((MainActivity) activity).selectProfileAddressesFragment();
                    break;
                }
            case R.id.addCCBtn:
                ccBtn = (Button)view;
                if(ccBtn.getText().equals("Add")){
                    Fragment creditFragment = Fragment.instantiate(activity, CreditCardFragment.class.getName());
                    ((MainActivity)activity).navigateToFragment(creditFragment);
                    break;
                } else {
                    ((MainActivity) activity).selectProfileCreditCardsFragment();
                    break;
                }
        }
    }
}