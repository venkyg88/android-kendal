package app.staples.mobile.cfa.profile;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.crittercism.app.Crittercism;
import app.staples.mobile.cfa.MainActivity;
import app.staples.mobile.cfa.R;
import app.staples.mobile.cfa.widget.ActionBar;
import com.staples.mobile.common.access.Access;
import com.staples.mobile.common.access.easyopen.api.EasyOpenApi;
import com.staples.mobile.common.access.easyopen.model.ApiError;
import com.staples.mobile.common.access.easyopen.model.EmptyResponse;
import com.staples.mobile.common.access.easyopen.model.member.AddCreditCard;
import com.staples.mobile.common.access.easyopen.model.member.AddCreditCardPOW;
import com.staples.mobile.common.access.easyopen.model.member.CCDetails;
import com.staples.mobile.common.access.easyopen.model.member.CreditCardId;
import com.staples.mobile.common.access.easyopen.model.member.Member;
import com.staples.mobile.common.access.easyopen.model.member.POWResponse;
import com.staples.mobile.common.access.easyopen.model.member.UpdateCreditCard;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class CreditCardFragment extends Fragment implements View.OnClickListener{

    private static final String TAG = CreditCardFragment.class.getSimpleName();

    Button addCCBtn;
    String creditCardNumber;
    String cardType;
    String expirationMonth;
    String expirationYear;
    String encryptedPacket;
    String expDate;

    EditText cardNumberET;
    ImageView cardImage;
    Button cancelCCBtn;
    EditText expirationMonthET;
    EditText expirationYearET;

    CCDetails creditCard;
    EasyOpenApi easyOpenApi;
    MainActivity activity;
    String creditCardId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        Crittercism.leaveBreadcrumb("CreditCardFragment:onCreateView(): Displaying the Credit Card screen.");
        activity = (MainActivity)getActivity();
        Resources r = getResources();

        View view = inflater.inflate(R.layout.add_creditcard_fragment, container, false);
        cardNumberET = (EditText) view.findViewById(R.id.cardNumber);
        expirationMonthET = (EditText) view.findViewById(R.id.expiration_month);
        expirationYearET = (EditText) view.findViewById(R.id.expiration_year);

        expirationMonthET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                String input = editable.toString();
                if (editable.length() == 1) {
                    int month = Integer.parseInt(input);
                    if (month > 1) {
                        expirationMonthET.setText("0" + expirationMonthET.getText().toString());
                        expirationYearET.requestFocus();
                    }

                } else if (editable.length() == 2) {
                    int month = Integer.parseInt(input);
                    if (month <= 12) {
                        expirationYearET.requestFocus();
                    } else {
                        activity.showErrorDialog("Please check the expiration month");
                    }
                } else {
                }

            }
        });

        expirationMonthET.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    CreditCard.Type ccType = CreditCard.Type.detect(cardNumberET.getText().toString().replaceAll(" ", ""));
                    if (ccType != CreditCard.Type.UNKNOWN) {
                        cardImage.setImageResource(ccType.getImageResource());
                    }
                }
            }
        });

        cardImage = (ImageView) view.findViewById(R.id.card_image);

        Bundle args = getArguments();
        if(args != null) {
             creditCard = (CCDetails)args.getSerializable("creditCardData");
            if(creditCard != null) {
                cardNumberET.setHint(r.getString(R.string.card_ending_in) + " " + creditCard.getCardNumber());
                cardType = creditCard.getCardType();
                cardImage.setImageResource(CreditCard.Type.matchOnApiName(cardType).getImageResource());

                if(creditCard.getExpirationMonth().length() == 1) {
                    expirationMonthET.setText("0" + creditCard.getExpirationMonth());
                }else{
                    expirationMonthET.setText(creditCard.getExpirationMonth());
                }

                expirationYearET.setText(creditCard.getExpirationYear().substring(2, 4));
                creditCardId = creditCard.getCreditCardId();
            }
        }
        cardNumberET.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                switch (actionId) {
                    case EditorInfo.IME_ACTION_NEXT:
                        validate();
                        expirationMonthET.requestFocus();
                        return (true);
                    case EditorInfo.IME_NULL:
                        Log.d(TAG, "Got an enter key");
                        if (event.getAction() == KeyEvent.ACTION_DOWN) {
                            validate();
                        }
                        return (true);
                }
                return (false);
            }
        });
        cardNumberET.setFilters(new InputFilter[] { new CcNumberInputFilter()});

        easyOpenApi = Access.getInstance().getEasyOpenApi(true);

        addCCBtn = (Button) view.findViewById(R.id.addCCBtn);
        addCCBtn.setOnClickListener(this);
        cancelCCBtn = (Button) view.findViewById(R.id.address_cancel);
        cancelCCBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().popBackStack();
            }
        });

        return (view);
    }

    private boolean validate() {
        CreditCard.Type ccType = CreditCard.Type.detect(cardNumberET.getText().toString().replaceAll(" ", ""));
        if (ccType != CreditCard.Type.UNKNOWN) {
            cardImage.setImageResource(ccType.getImageResource());
            return (true);
        }
        return(false);
    }

    @Override
    public void onResume() {
        super.onResume();
        ActionBar.getInstance().setConfig(ActionBar.Config.ADDCARD);
    }

    private boolean validateCC (String creditCardNumber, String expirationMonth, String expirationYear) {
        CreditCard card = new CreditCard(null, creditCardNumber);
        if(!card.isChecksumValid()) {
            activity.showErrorDialog(R.string.checksum_validation);
            return false;
        }

        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);
        int currentMonth = calendar.get(Calendar.MONTH);

        if(Integer.parseInt(expirationYear) <= currentYear && Integer.parseInt(expirationMonth) < currentMonth) {
            activity.showErrorDialog("Please check the expiration month & year");
            return false;
        }

        // validate card type
        cardType = CreditCard.Type.detect(creditCardNumber).getName();
        if (TextUtils.isEmpty(cardType)) {
            activity.showErrorDialog(R.string.cc_number_unrecognized);
            return false;
        }

        return true;
    }

    @Override
    public void onClick(View view) {
        activity.hideSoftKeyboard();

        // ensure credit card number entered
        creditCardNumber = cardNumberET.getText().toString().replaceAll(" ", "");
        expirationMonth = expirationMonthET.getText().toString();
        expirationYear = expirationYearET.getText().toString();

        if (TextUtils.isEmpty(creditCardNumber)) {
            activity.showErrorDialog(R.string.all_fields_required);
            return;
        }

        if(TextUtils.isEmpty(expirationMonth) && TextUtils.isEmpty(expirationYear)) {
            activity.showErrorDialog(R.string.all_fields_required);
            return;
        }

        expirationYear = "20" + expirationYear;

        if(!validateCC(creditCardNumber, expirationMonth, expirationYear)) {
            return;
        }

        final AddCreditCardPOW creditCard = new AddCreditCardPOW(creditCardNumber, cardType.toUpperCase());
        List<AddCreditCardPOW> ccList = new ArrayList<AddCreditCardPOW>();
        ccList.add(creditCard);
        Log.i(TAG, "Card " + creditCardNumber);
        Log.i(TAG, "CCN " + cardType);

        activity.showProgressIndicator();
        EasyOpenApi powApi = Access.getInstance().getPOWApi(true);
        powApi.addCreditPOWCall(ccList, new Callback<List<POWResponse>>() {

            @Override
            public void success(List<POWResponse> powList, Response response) {
                Log.i(TAG, "Packet " + powList.get(0).getPacket());
                Log.i(TAG, "Status " + powList.get(0).getStatus());
                encryptedPacket = powList.get(0).getPacket();

                if(encryptedPacket.isEmpty()) {
                    activity.hideProgressIndicator();
                    activity.showErrorDialog(R.string.cc_encryption_failure);
                    Log.i(TAG, "Success " + response.getUrl());
                }
                else if(creditCardId != null) {
                    UpdateCreditCard updatedCard= new UpdateCreditCard(cardType, encryptedPacket, expirationMonth, expirationYear, "notes", creditCardId);
                    easyOpenApi.updateMemberCreditCard(updatedCard, new Callback<EmptyResponse>() {
                        @Override
                        public void success(EmptyResponse empty, Response response) {
                            (new ProfileDetails()).refreshProfile(new ProfileDetails.ProfileRefreshCallback() {
                                @Override public void onProfileRefresh(Member member, String errMsg) {
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
                            (new ProfileDetails()).refreshProfile(new ProfileDetails.ProfileRefreshCallback() {
                                @Override public void onProfileRefresh(Member member, String errMsg) {
                                    activity.hideProgressIndicator();
                                    activity.showNotificationBanner(R.string.cc_added);
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
                            Log.i(TAG, "Add CC Fail Message " + error.getMessage());
                            Log.i(TAG, "Url " + error.getUrl());
                        }
                    } );
                }
            }

            @Override
            public void failure(RetrofitError error) {
                ((MainActivity)activity).hideProgressIndicator();
                activity.showErrorDialog(ApiError.getErrorMessage(error));
                Log.i(TAG, "Fail Response POW " + error.getUrl() + " " + error.getMessage());
            }
        });
    }
}
