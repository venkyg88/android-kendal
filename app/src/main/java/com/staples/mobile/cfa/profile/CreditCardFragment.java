package com.staples.mobile.cfa.profile;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.staples.mobile.cfa.MainActivity;
import com.staples.mobile.cfa.R;
import com.staples.mobile.cfa.widget.ActionBar;
import com.staples.mobile.common.access.Access;
import com.staples.mobile.common.access.easyopen.api.EasyOpenApi;
import com.staples.mobile.common.access.easyopen.model.ApiError;
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

    private static final String TAG = "CreditCardFragment";

    Button addCCBtn;
    String creditCardNumber;
    String cardType;
    String expirationMonth;
    String expirationYear;
    String encryptedPacket;
    EditText cardNumberET;
    EditText expDateET;
    ImageView cardImage;
    Button cancelCCBtn;

    CCDetails creditCard;
    EasyOpenApi easyOpenApi;
    MainActivity activity;
    String creditCardId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        Log.d(TAG, "onCreateView()");
        activity = (MainActivity)getActivity();

        View view = inflater.inflate(R.layout.add_creditcard_fragment, container, false);
        cardNumberET = (EditText) view.findViewById(R.id.cardNumber);
        expDateET = (EditText) view.findViewById(R.id.expirationDate);
        cardImage = (ImageView) view.findViewById(R.id.card_image);


        Bundle args = getArguments();
        if(args != null) {
             creditCard = (CCDetails)args.getSerializable("creditCardData");
            if(creditCard != null) {
                cardNumberET.setHint("Card ending in: " + creditCard.getCardNumber());
                cardType = creditCard.getCardType();
                cardImage.setImageResource(CreditCard.Type.matchOnApiName(cardType).getImageResource());
                expDateET.setVisibility(View.VISIBLE);
                expDateET.setText(creditCard.getExpirationMonth() + "/" +creditCard.getExpirationYear().substring(2,4));
                creditCardId = creditCard.getCreditCardId();
            }
        }
        cardNumberET.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    expDateET.setVisibility(View.VISIBLE);
                    expDateET.requestFocus();
                    CreditCard.Type ccType = CreditCard.Type.detect(cardNumberET.getText().toString());
                    if (ccType != CreditCard.Type.UNKNOWN) {
                        cardImage.setImageResource(ccType.getImageResource());
                        cardType = ccType.getName();
                    }
                    handled = true;
                }
                return handled;
            }
        });

        expDateET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(start == 1 && before < start) {
                    expDateET.setText(s+"/");
                    expDateET.setSelection(expDateET.getText().length());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }});

        easyOpenApi = Access.getInstance().getEasyOpenApi(true);

        addCCBtn = (Button) view.findViewById(R.id.addCCBtn);
        addCCBtn.setOnClickListener(this);
        cancelCCBtn = (Button) view.findViewById(R.id.cancelCCBtn);
        cancelCCBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().popBackStack();
            }
        });

        return (view);
    }

    @Override
    public void onResume() {
        super.onResume();
        ActionBar.getInstance().setConfig(ActionBar.Config.ADDCARD);
    }

    public void hideKeyboard(View view)
    {
        InputMethodManager keyboard = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        keyboard.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    @Override
    public void onClick(View view) {
        hideKeyboard(view);
        activity.showProgressIndicator();
        creditCardNumber = cardNumberET.getText().toString();
        String expirationDate = expDateET.getText().toString();
        if(expirationDate.length() != 5) {
            activity.showErrorDialog(R.string.check_exp_date);
            return;
        }
        else {
            expirationMonth = expirationDate.substring(0,2);
            expirationYear = "20" + expirationDate.substring(3,5);
        }

        cardType = CreditCard.Type.detect(creditCardNumber).getName();

        if(!creditCardNumber.isEmpty() && !cardType.isEmpty()){
            final AddCreditCardPOW creditCard = new AddCreditCardPOW(creditCardNumber, cardType.toUpperCase());
            List<AddCreditCardPOW> ccList = new ArrayList<AddCreditCardPOW>();
            ccList.add(creditCard);
            Log.i("Card", creditCardNumber);
            Log.i("CCN",cardType);

            EasyOpenApi powApi = Access.getInstance().getPOWApi(true);
            powApi.addCreditPOWCall(ccList, new Callback<List<POWResponse>>() {

                @Override
                public void success(List<POWResponse> powList, Response response) {
                    Log.i("packet", powList.get(0).getPacket());
                    Log.i("status",powList.get(0).getStatus());
                    encryptedPacket = powList.get(0).getPacket();

                    if(encryptedPacket.isEmpty()) {
                        activity.hideProgressIndicator();
                        activity.showErrorDialog(R.string.cc_encryption_failure);
                        Log.i("Success", response.getUrl());
                    }
                    else if(creditCardId != null) {
                        UpdateCreditCard updatedCard= new UpdateCreditCard(cardType, encryptedPacket, expirationMonth, expirationYear, "notes", creditCardId);
                        easyOpenApi.updateMemberCreditCard(updatedCard, new Callback<Response>() {
                            @Override
                            public void success(Response response, Response response2) {
                                (new ProfileDetails()).refreshProfile(new ProfileDetails.ProfileRefreshCallback() {
                                    @Override public void onProfileRefresh(Member member) {
                                        activity.hideProgressIndicator();
                                        activity.showNotificationBanner(R.string.cc_updated);
                                        FragmentManager fm = getFragmentManager();
                                        if (fm != null) {
                                            fm.popBackStack(); // this will take us back to one of the many places that could have opened this page
                                        }
                                    }
                                });
                            }

                            @Override
                            public void failure(RetrofitError error) {
                                activity.hideProgressIndicator();
                                activity.showErrorDialog(ApiError.getErrorMessage(error));
                            }
                        });
                    }
                    else {
                        AddCreditCard addCC = new AddCreditCard(cardType, encryptedPacket, expirationMonth, expirationYear, "notes");
                        easyOpenApi.addMemberCreditCard(addCC, new Callback<CreditCardId>() {
                            @Override
                            public void success(CreditCardId creditCardID, Response response) {
                                Log.i("Success", creditCardID.getCreditCardId());
                                activity.hideProgressIndicator();
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
                                activity.hideProgressIndicator();
                                activity.showErrorDialog(ApiError.getErrorMessage(error));
                                Log.i("Add CC Fail Message", error.getMessage());
                                Log.i("url", error.getUrl());
                            }
                        } );
                    }
                }

                @Override
                public void failure(RetrofitError error) {
                    ((MainActivity)activity).hideProgressIndicator();
                    activity.showErrorDialog(ApiError.getErrorMessage(error));
                    Log.i("Fail Response POW", error.getUrl() + error.getMessage());
                }
            });
        } else {
            activity.hideProgressIndicator();
            activity.showErrorDialog(R.string.all_fields_required);
        }
    }

}
