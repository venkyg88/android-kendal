package com.staples.mobile.cfa.profile;

import android.app.Activity;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.staples.mobile.cfa.R;
import com.staples.mobile.cfa.BaseFragment;
import com.staples.mobile.cfa.MainActivity;
import com.staples.mobile.cfa.login.LoginHelper;
import com.staples.mobile.cfa.widget.LinearLayoutWithProgressOverlay;
import com.staples.mobile.common.access.easyopen.api.EasyOpenApi;
import com.staples.mobile.common.access.easyopen.model.member.*;

import java.util.List;

public class ProfileFragment extends BaseFragment implements ProfileDetails.ProfileRefreshCallback, View.OnClickListener{
    private static final String TAG = "ProfileFragment";

    private static final String RECOMMENDATION = "v1";
    private static final String STORE_ID = "10001";
    private static final String CLIENT_ID = LoginHelper.CLIENT_ID;
    private static final String LOCALE = "en_US";

    private EasyOpenApi easyOpenApi;
    Button shippingBtn;
    Button ccBtn;
    private LinearLayoutWithProgressOverlay profileLayout;
    Activity activity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {

        Log.d(TAG, "onCreateView()");
        activity = getActivity();
        View view = inflater.inflate(R.layout.profile_fragment, container, false);
        profileLayout = (LinearLayoutWithProgressOverlay) view.findViewById(R.id.profile_fragment_content);
        profileLayout.setCartProgressOverlay(view.findViewById(R.id.profile_progress_overlay));

        shippingBtn = (Button) view.findViewById(R.id.addShippingBtn);
        ccBtn = (Button) view.findViewById(R.id.addCCBtn);
        shippingBtn.setOnClickListener(this);
        ccBtn.setOnClickListener(this);

        showProgressIndicator();
        new ProfileDetails().refreshProfile(this);

        return (view);
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
        profileLayout.getProgressIndicator().showProgressIndicator();
    }

    private void hideProgressIndicator() {
        profileLayout.getProgressIndicator().hideProgressIndicator();
    }


    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.addShippingBtn:
                if(ProfileDetails.hasAddress()) {
                    Fragment shippingListFragment = Fragment.instantiate(activity, ShippingListFragment.class.getName());
                    ((MainActivity) activity).navigateToFragment(shippingListFragment);
                    break;
                }
                else{
                    Fragment shippingFragment = Fragment.instantiate(activity, ShippingFragment.class.getName());
                    ((MainActivity) activity).navigateToFragment(shippingFragment);
                    break;
                }


            case R.id.addCCBtn:
                if(ProfileDetails.hasPaymentMethod()) {
                    Fragment cardListFragment = Fragment.instantiate(activity, CreditCardListFragment.class.getName());
                    ((MainActivity) activity).navigateToFragment(cardListFragment);
                    break;
                }
                else{
                    Fragment cardFragment = Fragment.instantiate(activity, CreditCardFragment.class.getName());
                    ((MainActivity) activity).navigateToFragment(cardFragment);
                    break;
                }
        }
    }
}