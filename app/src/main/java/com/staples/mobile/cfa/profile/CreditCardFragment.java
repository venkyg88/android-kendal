package com.staples.mobile.cfa.profile;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.staples.mobile.cfa.MainActivity;
import com.staples.mobile.cfa.R;
import com.staples.mobile.cfa.login.LoginHelper;
import com.staples.mobile.common.access.Access;
import com.staples.mobile.common.access.easyopen.api.EasyOpenApi;
import com.staples.mobile.common.access.easyopen.model.member.AddCreditCard;
import com.staples.mobile.common.access.easyopen.model.member.AddCreditCardPOW;
import com.staples.mobile.common.access.easyopen.model.member.CCDetails;
import com.staples.mobile.common.access.easyopen.model.member.CreditCardId;
import com.staples.mobile.common.access.easyopen.model.member.Member;
import com.staples.mobile.common.access.easyopen.model.member.POWResponse;
import com.staples.mobile.common.access.easyopen.model.member.UpdateCreditCard;

import java.util.ArrayList;
import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by Avinash Dodda.
 */
public class CreditCardFragment extends Fragment implements View.OnClickListener{

    private static final String TAG = "Add Credit Card Fragment";
    private static final String RECOMMENDATION = "v1";
    private static final String STORE_ID = "10001";
    private static final String LOCALE = "en_US";
    private static final String CLIENT_ID = LoginHelper.CLIENT_ID;

    Button addCCBtn;
    Spinner spinner;
    String creditCardNumber;
    String cardType;
    String expirationMonth;
    String expirationYear;
    String encryptedPacket;
    EditText cardNumberET;
    EditText expMonthET;
    EditText expYearET;

    CCDetails creditCard;
    EasyOpenApi easyOpenApi;
    Activity activity;
    String creditCardId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        Log.d(TAG, "onCreateView()");
        activity = getActivity();

        View view = inflater.inflate(R.layout.add_creditcard_fragment, container, false);
        spinner = (Spinner) view.findViewById(R.id.card_type_spinner);
        cardNumberET = (EditText) view.findViewById(R.id.cardNumber);
        expMonthET = (EditText) view.findViewById(R.id.expirationMonth);
        expYearET = (EditText) view.findViewById(R.id.expirationYear);

        Bundle args = getArguments();
        if(args != null) {
             creditCard = (CCDetails)args.getSerializable("creditCardData");
            if(creditCard != null) {
                cardNumberET.setText(creditCard.getCardNumber());
                expMonthET.setText(creditCard.getExpirationMonth());
                expYearET.setText(creditCard.getExpirationYear());
                creditCardId = creditCard.getCreditCardId();
            }
        }
    
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.cardtype_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        easyOpenApi = Access.getInstance().getEasyOpenApi(true);

        addCCBtn = (Button) view.findViewById(R.id.addCCBtn);
        addCCBtn.setOnClickListener(this);

        return (view);
    }

    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity) activity).showActionBar(R.string.add_credit_card_title, 0, null);
    }

    public void hideKeyboard(View view)
    {
        InputMethodManager keyboard = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        keyboard.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    @Override
    public void onClick(View view) {
        hideKeyboard(view);
        ((MainActivity)activity).showProgressIndicator();
        creditCardNumber = cardNumberET.getText().toString();
        cardType = spinner.getSelectedItem().toString();
        expirationMonth = expMonthET.getText().toString();
        expirationYear = expYearET.getText().toString();

        if(!creditCardNumber.isEmpty() && !cardType.isEmpty()){
            final AddCreditCardPOW creditCard = new AddCreditCardPOW(creditCardNumber, cardType.toUpperCase());
            List<AddCreditCardPOW> ccList = new ArrayList<AddCreditCardPOW>();
            ccList.add(creditCard);

            easyOpenApi.addCreditPOWCallQA(ccList, RECOMMENDATION, CLIENT_ID, new Callback<List<POWResponse>>() {

                @Override
                public void success(List<POWResponse> powList, Response response) {
                    Log.i("packet", powList.get(0).getPacket());
                    Log.i("status", powList.get(0).getStatus());
                    encryptedPacket = powList.get(0).getPacket();

                    if(encryptedPacket.isEmpty()) {
                        Toast.makeText(getActivity(), "Credit card encryption failed" , Toast.LENGTH_LONG).show();
                    }
                    else if(creditCardId != null) {
                        UpdateCreditCard updatedCard= new UpdateCreditCard(cardType, encryptedPacket, expirationMonth, expirationYear, "notes", creditCardId);
                        easyOpenApi.updateMemberCreditCard(updatedCard, RECOMMENDATION, STORE_ID, LOCALE, CLIENT_ID, new Callback<Response>() {
                            @Override
                            public void success(Response response, Response response2) {
                                (new ProfileDetails()).refreshProfile(new ProfileDetails.ProfileRefreshCallback() {
                                    @Override public void onProfileRefresh(Member member) {
                                        ((MainActivity)activity).hideProgressIndicator();
                                        Toast.makeText(getActivity(), "Card Updated", Toast.LENGTH_LONG).show();
                                        FragmentManager fm = getFragmentManager();
                                        if (fm != null) {
                                            fm.popBackStack(); // this will take us back to one of the many places that could have opened this page
                                        }
                                    }
                                });
                            }

                            @Override
                            public void failure(RetrofitError error) {
                                ((MainActivity)activity).hideProgressIndicator();
                                Toast.makeText(getActivity(), error.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                    else {
                        AddCreditCard addCC = new AddCreditCard(cardType, encryptedPacket, expirationMonth, expirationYear, "notes");
                        easyOpenApi.addMemberCreditCard(addCC, RECOMMENDATION, STORE_ID, LOCALE, CLIENT_ID,new Callback<CreditCardId>() {
                            @Override
                            public void success(CreditCardId creditCardID, Response response) {
                                Log.i("Success", creditCardID.getCreditCardId());
                                ((MainActivity)activity).hideProgressIndicator();
                                Toast.makeText(getActivity(), "Credit Card Id: "+ creditCardID.getCreditCardId(), Toast.LENGTH_LONG).show();
                                (new ProfileDetails()).refreshProfile(new ProfileDetails.ProfileRefreshCallback() {
                                    @Override public void onProfileRefresh(Member member) {
                                        FragmentManager fm = getFragmentManager();
                                        if (fm != null) {
                                            fm.popBackStack(); // this will take us back to one of the many places that could have opened this page
                                        }
                                    }
                                });
                            }

                            @Override
                            public void failure(RetrofitError error) {
                                ((MainActivity)activity).hideProgressIndicator();
                                Log.i("Add CC Fail Message", error.getMessage());
                                Log.i("url", error.getUrl());
                            }
                        } );
                    }
                }

                @Override
                public void failure(RetrofitError error) {
                    ((MainActivity)activity).hideProgressIndicator();
                    Log.i("Fail Response POW", error.getUrl() + error.getMessage());
                }
            });
        }
    }

}
