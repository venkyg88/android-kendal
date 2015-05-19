package app.staples.mobile.cfa.profile;

import android.app.Fragment;
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

import app.staples.R;
import app.staples.mobile.cfa.MainActivity;
import app.staples.mobile.cfa.widget.ActionBar;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class CreditCardFragment extends Fragment implements Callback, ProfileDetails.ProfileRefreshCallback, View.OnClickListener, TextWatcher, View.OnFocusChangeListener, TextView.OnEditorActionListener {

    private static final String TAG = CreditCardFragment.class.getSimpleName();

    Button addCCBtn;
    String creditCardNumber;
    String cardType;
    String expirationMonth;
    String expirationYear;
    String expDate;

    EditText cardNumberET;
    ImageView cardImage;
    Button cancelCCBtn;
    EditText expirationMonthET;
    EditText expirationYearET;

    CCDetails creditCard;
    EasyOpenApi easyOpenApi;
    String creditCardId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        Crittercism.leaveBreadcrumb("CreditCardFragment:onCreateView(): Displaying the Credit Card screen.");
        Resources r = getResources();
        easyOpenApi = Access.getInstance().getEasyOpenApi(true);

        View view = inflater.inflate(R.layout.credit_card_fragment, container, false);
        cardNumberET = (EditText) view.findViewById(R.id.cardNumber);
        expirationMonthET = (EditText) view.findViewById(R.id.expiration_month);
        expirationYearET = (EditText) view.findViewById(R.id.expiration_year);
        cardImage = (ImageView) view.findViewById(R.id.card_image);
        addCCBtn = (Button) view.findViewById(R.id.add_cc_save);
        cancelCCBtn = (Button) view.findViewById(R.id.add_cc_cancel);

        expirationMonthET.addTextChangedListener(this);
        expirationMonthET.setOnFocusChangeListener(this);
        cardNumberET.setOnEditorActionListener(this);
        cardNumberET.setFilters(new InputFilter[]{new InputFilter.LengthFilter(19), new CcNumberInputFilter()});
        addCCBtn.setOnClickListener(this);
        cancelCCBtn.setOnClickListener(this);

        Bundle args = getArguments();
        if (args != null) {
            creditCard = (CCDetails) args.getSerializable("creditCardData");
            if (creditCard != null) {
                cardNumberET.setHint(r.getString(R.string.card_ending_in) + " " + creditCard.getCardNumber());
                cardType = creditCard.getCardType();
                cardImage.setImageResource(CreditCard.Type.matchOnApiName(cardType).getImageResource());

                if (creditCard.getExpirationMonth().length() == 1) {
                    expirationMonthET.setText("0" + creditCard.getExpirationMonth());
                } else {
                    expirationMonthET.setText(creditCard.getExpirationMonth());
                }

                expirationYearET.setText(creditCard.getExpirationYear().substring(2, 4));
                creditCardId = creditCard.getCreditCardId();
            }
        }

        return (view);
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (hasFocus) {
            CreditCard.Type ccType = CreditCard.Type.detect(cardNumberET.getText().toString().replaceAll(" ", ""));
            if (ccType != CreditCard.Type.UNKNOWN) {
                cardImage.setImageResource(ccType.getImageResource());
            }
        }
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
    }

    @Override
    public void afterTextChanged(Editable editable) {
        MainActivity activity = (MainActivity) getActivity();

        String input = editable.toString();
        if (editable.length() == 1) {
            try {
                int month = Integer.parseInt(input);
                if (month > 1) {
                    expirationMonthET.setText("0" + expirationMonthET.getText().toString());
                    expirationYearET.requestFocus();
                }
            } catch (NumberFormatException nfe) {
            }
        } else if (editable.length() == 2) {
            try {
                int month = Integer.parseInt(input);
                if (month <= 12) {
                    expirationYearET.requestFocus();
                } else {
                    activity.showErrorDialog("Please check the expiration month");
                }
            } catch (NumberFormatException nfe) {
            }

        } else {
        }

    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        switch(actionId) {
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

    @Override
    public void onResume() {
        super.onResume();
        ActionBar.getInstance().setConfig(ActionBar.Config.ADDCARD);
    }

    private boolean validate() {
        CreditCard.Type ccType = CreditCard.Type.detect(cardNumberET.getText().toString().replaceAll(" ", ""));
        if (ccType != CreditCard.Type.UNKNOWN) {
            cardImage.setImageResource(ccType.getImageResource());
            return (true);
        }
        return (false);
    }

    private boolean validateCC(String creditCardNumber, String expirationMonth, String expirationYear) {
        MainActivity activity = (MainActivity) getActivity();

        CreditCard card = new CreditCard(null, creditCardNumber);
        if (!card.isChecksumValid()) {
            activity.showErrorDialog(R.string.checksum_validation);
            return false;
        }

        // calender month returns between 0-11. so adding 1 to produce the error when entering the previous month
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);
        int currentMonth = calendar.get(Calendar.MONTH) + 1;

        try {
            if (Integer.parseInt(expirationYear) == currentYear) {
                if (Integer.parseInt(expirationMonth) == 0 || Integer.parseInt(expirationMonth) < currentMonth) {
                    activity.showErrorDialog(R.string.card_expiration_msg);
                    return false;
                }
            }
            if(Integer.parseInt(expirationYear) < currentYear) {
                activity.showErrorDialog(R.string.card_expiration_year);
                return false;
            }
        } catch (NumberFormatException nfe) {
        }


        // validate card type
        cardType = CreditCard.Type.detect(creditCardNumber).getName();
        if (TextUtils.isEmpty(cardType)) {
            activity.showErrorDialog(R.string.cc_number_unrecognized);
            return false;
        }

        return true;
    }

    private void updateCreditCard(String encryptedPacket) {
        UpdateCreditCard updatedCard = new UpdateCreditCard(cardType, encryptedPacket, expirationMonth, expirationYear, "notes", creditCardId);
        easyOpenApi.updateMemberCreditCard(updatedCard, this);
    }

    private void addCreditCard(String encryptedPacket) {
        AddCreditCard addCC = new AddCreditCard(cardType, encryptedPacket, expirationMonth, expirationYear, "notes");
        easyOpenApi.addMemberCreditCard(addCC, this);
    }

    @Override
    public void onProfileRefresh(Member member, String errMsg) {
        MainActivity activity = (MainActivity) getActivity();
        if (activity == null) return;

        activity.hideProgressIndicator();
        activity.showNotificationBanner(R.string.cc_updated);

        activity.popBackStack();
    }

    private String getEncryptedPacket(List list) {
        if (list!=null && list.size()>0) {
            Object obj = list.get(0);
            if (obj instanceof POWResponse) {
                String packet = ((POWResponse) obj).getPacket();
                if (packet != null && !packet.isEmpty()) {
                    return(packet);
                }
            }
        }
        return(null);
    }

    @Override
    public void success(Object obj, Response response) {
        MainActivity activity = (MainActivity) getActivity();
        if (activity == null) return;

        // Callback #1, encrypted credit card received
        if (obj instanceof List) {
            String encryptedPacket = getEncryptedPacket((List) obj);
            if (encryptedPacket==null) {
                activity.hideProgressIndicator();
                activity.showErrorDialog(R.string.cc_encryption_failure);
            } else if (creditCardId != null) {
                updateCreditCard(encryptedPacket);
            } else {
                addCreditCard(encryptedPacket);
            }
        }

        // Callback #2, added/updated credit card confirmed
        else if (obj instanceof CreditCardId) {
            (new ProfileDetails()).refreshProfile(this);
        }

        // Delete credit card confirmed
        else if (obj instanceof EmptyResponse) {
            (new ProfileDetails()).refreshProfile(this);
        }
    }

    @Override
    public void failure(RetrofitError error) {
        MainActivity activity = (MainActivity) getActivity();
        if (activity==null) return;

        activity.hideProgressIndicator();
        activity.showErrorDialog(ApiError.getErrorMessage(error));
    }

    @Override
    public void onClick(View view) {
        MainActivity activity = (MainActivity) getActivity();
        switch(view.getId()) {
            case R.id.add_cc_save:
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

                activity.showProgressIndicator();
                EasyOpenApi powApi = Access.getInstance().getPOWApi(true);
                powApi.addCreditPOWCall(ccList, this);
                break;

            case R.id.add_cc_cancel:
                activity.popBackStack();
                break;
        }
    }
}
