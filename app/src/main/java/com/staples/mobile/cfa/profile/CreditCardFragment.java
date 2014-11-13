package com.staples.mobile.cfa.profile;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.staples.mobile.R;
import com.staples.mobile.cfa.MainActivity;
import com.staples.mobile.cfa.login.LoginHelper;
import com.staples.mobile.common.access.Access;
import com.staples.mobile.common.access.easyopen.api.EasyOpenApi;
import com.staples.mobile.common.access.easyopen.model.member.AddCreditCard;
import com.staples.mobile.common.access.easyopen.model.member.AddCreditCardPOW;
import com.staples.mobile.common.access.easyopen.model.member.CreditCardID;
import com.staples.mobile.common.access.easyopen.model.member.POWResponse;

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
    Button addCCBtn;
    Spinner spinner;
    String creditCardNumber;
    String cardType;
    String expirationMonth;
    String expirationYear;
    EasyOpenApi easyOpenApi;
    String encryptedPacket;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {

        Log.d(TAG, "onCreateView()");
        View view = inflater.inflate(R.layout.addcc_fragment, container, false);
        spinner = (Spinner) view.findViewById(R.id.card_type_spinner);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.cardtype_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        easyOpenApi = Access.getInstance().getPOWApi();

        addCCBtn = (Button) view.findViewById(R.id.addCCBtn);
        addCCBtn.setOnClickListener(this);

        return (view);
    }

    @Override
    public void onClick(View view) {
        creditCardNumber = ((EditText) getView().findViewById(R.id.cardNumber)).getText().toString();
        cardType = spinner.getSelectedItem().toString().toUpperCase();
        expirationMonth = ((EditText) getView().findViewById(R.id.expirationMonth)).getText().toString();
        expirationYear = ((EditText) getView().findViewById(R.id.expirationYear)).getText().toString();

        if(!creditCardNumber.isEmpty() && !cardType.isEmpty()){
            AddCreditCardPOW creditCard = new AddCreditCardPOW(creditCardNumber, cardType);
            List<AddCreditCardPOW> ccList = new ArrayList<AddCreditCardPOW>();
            ccList.add(creditCard);

            easyOpenApi.addCreditPOWCall(ccList, new Callback<POWResponse[]>() {
                @Override
                public void success(POWResponse[] powList, Response response) {
                    Log.i("packet", powList[0].getPacket());
                    encryptedPacket = powList[0].getPacket();
                    if(!encryptedPacket.isEmpty()) {
                        AddCreditCard addCC = new AddCreditCard(cardType, encryptedPacket, expirationMonth, expirationYear, "notes");
                        easyOpenApi = Access.getInstance().getEasyOpenApi(true);
                        easyOpenApi.addMemberCreditCard(addCC,RECOMMENDATION,STORE_ID,LOCALE, LoginHelper.CLIENT_ID,new Callback<CreditCardID>() {
                            @Override
                            public void success(CreditCardID creditCardID, Response response) {
                                Log.i("Success", creditCardID.getCreditCardId());
                                Toast.makeText(getActivity(), "Credit Card Id: "+ creditCardID.getCreditCardId(), Toast.LENGTH_LONG).show();
                                Fragment profileFragment = Fragment.instantiate(getActivity(), ProfileFragment.class.getName());
                                ((MainActivity)getActivity()).navigateToFragment(profileFragment);
                            }

                            @Override
                            public void failure(RetrofitError error) {
                                Log.i("Add CC Fail Message", error.getMessage());
                                Log.i("url", error.getUrl());
                            }
                        } );
                    }
                }

                @Override
                public void failure(RetrofitError error) {
                    Log.i("Fail Response POW", error.getUrl() + error.getMessage());
                }
            });
        }
    }

}
