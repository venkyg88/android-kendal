package com.staples.mobile.cfa.profile;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.staples.mobile.cfa.MainActivity;
import com.staples.mobile.cfa.R;
import com.staples.mobile.cfa.login.LoginHelper;
import com.staples.mobile.common.access.Access;
import com.staples.mobile.common.access.easyopen.api.EasyOpenApi;
import com.staples.mobile.common.access.easyopen.model.member.CCDetails;
import com.staples.mobile.common.access.easyopen.model.member.Member;

import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by Avinash Dodda.
 */

public class CreditCardArrayAdapter extends ArrayAdapter<CCDetails> implements View.OnClickListener{

    private static final String RECOMMENDATION = "v1";
    private static final String STORE_ID = "10001";
    private static final String CLIENT_ID = LoginHelper.CLIENT_ID;
    private static final String LOCALE = "en_US";

    private final Context context;
    private final List<CCDetails> values;
    private String selectedCreditCardId;
    ImageButton optionButton;
    EasyOpenApi easyOpenApi;

    public CreditCardArrayAdapter(Context context, List<CCDetails> values, String selectedCreditCardId) {
        super(context, R.layout.list_view_row, values);
        this.context = context;
        this.values = values;
        this.selectedCreditCardId = selectedCreditCardId;
        easyOpenApi = Access.getInstance().getEasyOpenApi(true);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.list_view_row, parent, false);

        CCDetails creditCard = values.get(position);
        String cardNumber;
        if (creditCard.getCardNumber().length() > 4) {
            cardNumber = creditCard.getCardNumber().substring(creditCard.getCardNumber().length() - 4);
        } else {
            cardNumber = creditCard.getCardNumber();
        }
        String tmpCard = creditCard.getCardType() + " ending in " + cardNumber + "\n" +
                "Exp. " + creditCard.getExpirationMonth() + "/" + creditCard.getExpirationYear();

        TextView ccText = (TextView) rowView.findViewById(R.id.rowItemText);
        ccText.setText(tmpCard);
        ccText.setTag(position);
        ccText.setOnClickListener(this);

        optionButton = (ImageButton) rowView.findViewById(R.id.listOptions);
        optionButton.setTag(position);
        optionButton.setOnClickListener(this);

        if (selectedCreditCardId != null) {
            View selectionImageView = rowView.findViewById(R.id.selectionImage);
            if (selectedCreditCardId.equals(creditCard.getCreditCardId())) {
                selectionImageView.setVisibility(View.VISIBLE); // visible
            } else {
                selectionImageView.setVisibility(View.INVISIBLE); // invisible but taking up space
            }
        }
        return rowView;
    }
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.rowItemText:
                final int position = (Integer) view.getTag();
                String paymentMethodId = values.get(position).getCreditCardId();
                if (ProfileDetails.paymentMethodSelectionListener != null) {
                    ProfileDetails.paymentMethodSelectionListener.onPaymentMethodSelected(paymentMethodId);
                } else {
                    Toast.makeText(context, paymentMethodId, Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.listOptions:
                final int position1 = (Integer) view.getTag();
                //Creating the instance of PopupMenu
                PopupMenu popup = new PopupMenu(context, view);
                //Inflating the Popup using xml file
                popup.getMenuInflater()
                        .inflate(R.menu.list_menu_options, popup.getMenu());

                //registering popup with OnMenuItemClickListener
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.updateListItem:
                                updateCreditCard(position1);
                                return true;
                            case R.id.deleteListItem:
                                deleteCreditCard(position1);
                                return true;
                            default:
                                return true;
                        }
                    }
                });
                popup.show(); //showing popup menu
                break;
        }
    }

    public void updateCreditCard(int position) {
        Fragment creditFragment = Fragment.instantiate(context, CreditCardFragment.class.getName());
        Bundle bundle = new Bundle();
        bundle.putSerializable("creditCardData", values.get(position));
        creditFragment.setArguments(bundle);
        ((MainActivity)context).navigateToFragment(creditFragment);
    }

    public void deleteCreditCard(final int position) {
        String creditCardId = values.get(position).getCreditCardId();
        ((MainActivity)context).showProgressIndicator();
        easyOpenApi.deleteMemberCreditCard(RECOMMENDATION, STORE_ID, creditCardId, LOCALE, "json", CLIENT_ID, new Callback<Response>() {

            @Override
            public void success(Response response, Response response2) {
                (new ProfileDetails()).refreshProfile(new ProfileDetails.ProfileRefreshCallback() {
                    @Override
                    public void onProfileRefresh(Member member) {
                        ((MainActivity) context).hideProgressIndicator();
                        values.remove(position);
                        notifyDataSetChanged();
                        Toast.makeText(context, "Credit card deleted", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void failure(RetrofitError error) {
                ((MainActivity)context).hideProgressIndicator();
                Toast.makeText(context, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}


