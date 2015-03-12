package com.staples.mobile.cfa.profile;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.staples.mobile.cfa.MainActivity;
import com.staples.mobile.cfa.R;
import com.staples.mobile.cfa.analytics.Tracker;
import com.staples.mobile.cfa.checkout.ConfirmationFragment;
import com.staples.mobile.cfa.home.ConfiguratorFragment;
import com.staples.mobile.cfa.widget.ActionBar;
import com.staples.mobile.common.access.easyopen.api.EasyOpenApi;
import com.staples.mobile.common.access.easyopen.model.member.Address;
import com.staples.mobile.common.access.easyopen.model.member.CCDetails;
import com.staples.mobile.common.access.easyopen.model.member.Member;

import java.util.List;

public class ProfileFragment extends Fragment implements View.OnClickListener{
    public static final String TAG = "ProfileFragment";

    private EasyOpenApi easyOpenApi;
    Button shippingBtn;
    Button ccBtn;
    TextView addressTV;
    TextView creditCardTv;
    MainActivity activity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        activity = (MainActivity) getActivity();

        View view = inflater.inflate(R.layout.profile_fragment, container, false);

        shippingBtn = (Button) view.findViewById(R.id.addShippingBtn);
        ccBtn = (Button) view.findViewById(R.id.addCCBtn);
        addressTV = (TextView) view.findViewById(R.id.addressET);
        creditCardTv = (TextView) view.findViewById(R.id.ccET);
        shippingBtn.setOnClickListener(this);
        ccBtn.setOnClickListener(this);
        loadProfile(ProfileDetails.getMember(), view);

        return (view);
    }

    @Override
    public void onResume() {
        super.onResume();
        ActionBar.getInstance().setConfig(ActionBar.Config.PROFILE);
        Tracker.getInstance().trackStateForProfile(); // Analytics
    }

    private void loadProfile(Member member, View view) {
        if (member==null) return;

        String email = member.getEmailAddress();
        String userName = member.getUserName();

        if(member.getRewardsNumber() != null && member.getRewardDetails() != null) {
            System.out.println("member.getAmountRewards():" + member.getRewardDetails().get(0).getAmountRewards());
        }

        if (email!=null)
            ((TextView) view.findViewById(R.id.emailProfile)).setText(email);
        if (userName!=null)
            ((TextView) view.findViewById(R.id.userNameProfile)).setText(userName);

        List<Address> addresses = member.getAddress();
        if (addresses!=null) {
            int addressCount = addresses.size();
            Address address = addresses.get(0);
            if (address != null) {
                String tmpAddress = address.getAddress1() + "\n" + address.getCity() + ", " + address.getState() + " " + address.getZipCode();
                addressTV.setText(tmpAddress);
                if(addressCount > 1) {
                    shippingBtn.setText(addressCount-1 + " more");
                }
                else {
                    addressTV.setText(tmpAddress);
                    shippingBtn.setText("+ Add");
                }
            }
        }else {
            addressTV.setText("Shipping Addresses");
            shippingBtn.setText("+ Add");
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
                creditCardTv.setText(tmpCreditCard);
                if(creditCardCount > 1) {
                    ccBtn.setText(creditCardCount-1 + " more");
                }
                else {
                    creditCardTv.setText(tmpCreditCard);
                    ccBtn.setText("+ Add");
                }
              }
        }else {
            creditCardTv.setText("Credit Cards");
            ccBtn.setText("+ Add");
        }
    }



    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.addShippingBtn:
                shippingBtn = (Button) view;
                if (shippingBtn.getText().equals("Add")) {
                    Fragment addressFragment = Fragment.instantiate(activity, AddressFragment.class.getName());
                    activity.navigateToFragment(addressFragment);
                    break;
                } else {
                    activity.selectProfileAddressesFragment();
                    break;
                }
            case R.id.addCCBtn:
                ccBtn = (Button)view;
                if(ccBtn.getText().equals("Add")){
                    Fragment creditFragment = Fragment.instantiate(activity, CreditCardFragment.class.getName());
                    activity.navigateToFragment(creditFragment);
                    break;
                } else {
                    activity.selectProfileCreditCardsFragment();
                    break;
                }
        }
    }
}
