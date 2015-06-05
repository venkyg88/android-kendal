package app.staples.mobile.cfa.profile;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.crittercism.app.Crittercism;
import com.staples.mobile.common.access.easyopen.api.EasyOpenApi;
import com.staples.mobile.common.access.easyopen.model.member.Address;
import com.staples.mobile.common.access.easyopen.model.member.CCDetails;
import com.staples.mobile.common.access.easyopen.model.member.Member;
import com.staples.mobile.common.analytics.Tracker;

import java.util.List;

import app.staples.R;
import app.staples.mobile.cfa.MainActivity;
import app.staples.mobile.cfa.widget.ActionBar;

public class ProfileFragment extends Fragment implements View.OnClickListener{
    private static final String TAG = ProfileFragment.class.getSimpleName();

    private EasyOpenApi easyOpenApi;
    TextView shippingBtn;
    TextView ccBtn;
    TextView addressTV;
    TextView creditCardTv;
    ImageView cardImage;
    RelativeLayout addressLayout;
    RelativeLayout creditcardLayout;
    MainActivity activity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        Crittercism.leaveBreadcrumb("ProfileFragment:onCreateView(): Displaying the User Profile screen.");
        activity = (MainActivity) getActivity();

        View view = inflater.inflate(R.layout.profile_fragment, container, false);
        view.setTag(this);

        shippingBtn = (TextView) view.findViewById(R.id.addShippingBtn);
        ccBtn = (TextView) view.findViewById(R.id.addCCBtn);
        addressTV = (TextView) view.findViewById(R.id.addressET);
        creditCardTv = (TextView) view.findViewById(R.id.ccET);
        cardImage = (ImageView) view.findViewById(R.id.card_image_profile);
        addressLayout = (RelativeLayout) view.findViewById(R.id.address_layout);
        creditcardLayout = (RelativeLayout) view.findViewById(R.id.credit_card_layout);

        addressLayout.setOnClickListener(this);
        creditcardLayout.setOnClickListener(this);

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
                    shippingBtn.setText(addressCount-1 + "+");
                }
                else {
                    addressTV.setText(tmpAddress);
                    shippingBtn.setText("+");
                }
            }
        }else {
            addressTV.setText("Shipping Addresses");
            shippingBtn.setText("+");
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
                cardImage.setVisibility(View.VISIBLE);
                cardImage.setImageResource(CreditCard.Type.matchOnApiName(creditCard.getCardType()).getImageResource());
                creditCardTv.setText("*"+cardNumber);
                if(creditCardCount > 1) {
                    ccBtn.setText(creditCardCount-1 + "+");
                }
                else {
                    ccBtn.setText("+");
                }
              }
        }else {
            cardImage.setVisibility(View.GONE);
            creditCardTv.setText("Credit Cards");
            ccBtn.setText("+");
        }
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.address_layout:
                activity.selectProfileAddressesFragment();
                break;
            case R.id.credit_card_layout:
                activity.selectProfileCreditCardsFragment();
                break;
        }
    }
}
