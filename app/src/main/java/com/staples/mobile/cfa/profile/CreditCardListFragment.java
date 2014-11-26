package com.staples.mobile.cfa.profile;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.staples.mobile.R;
import com.staples.mobile.cfa.BaseFragment;
import com.staples.mobile.cfa.MainActivity;
import com.staples.mobile.common.access.easyopen.model.member.CCDetails;

import java.util.List;

/**
 * Created by Avinash Dodda.
 */
public class CreditCardListFragment extends BaseFragment implements View.OnClickListener {
    private static final String TAG = "CreditCardList Fragment";
    ListView listview;
    Button addBtn;
    List<CCDetails> cardList;
    Activity activity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        Log.d(TAG, "onCreateView()");

        View view = inflater.inflate(R.layout.list_fragment, container, false);
        listview = (ListView) view.findViewById(R.id.profileListView);
        addBtn = (Button) view.findViewById(R.id.listAddButton);
        addBtn.setText("Add Credit Card");
        addBtn.setOnClickListener(this);
        cardList = ProfileDetails.getMember().getCreditCard();
        activity = getActivity();

        final CardArrayAdapter adapter = new CardArrayAdapter(activity,
                cardList, ProfileDetails.currentPaymentMethodId);
        listview.setAdapter(adapter);
        registerForContextMenu(listview);

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> a, View v, int position, long id) {
                String paymentMethodId = cardList.get(position).getCreditCardId();
                if (ProfileDetails.paymentMethodSelectionListener != null) {
                    ProfileDetails.paymentMethodSelectionListener.onPaymentMethodSelected(paymentMethodId);
                } else {
                    Toast.makeText(activity, paymentMethodId, Toast.LENGTH_LONG).show();
                }
            }
        });
        return (view);
    }

    @Override
    public void onClick(View view) {
        Fragment cardFragment = Fragment.instantiate(activity, CreditCardFragment.class.getName());
        ((MainActivity) activity).navigateToFragment(cardFragment);
    }
}

class CardArrayAdapter extends ArrayAdapter<CCDetails> {
    private final Context context;
    private final List<CCDetails> values;
    private String creditCardId;

    public CardArrayAdapter(Context context, List<CCDetails> values, String creditCardId) {
        super(context, R.layout.list_view_row, values);
        this.context = context;
        this.values = values;
        this.creditCardId = creditCardId;
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
        if (creditCardId != null) {
            View selectionImageView = rowView.findViewById(R.id.selectionImage);
            if (creditCardId.equals(creditCard.getCreditCardId())) {
                selectionImageView.setVisibility(View.VISIBLE);
            } else {
                selectionImageView.setVisibility(View.INVISIBLE);
            }
        }
        return rowView;
    }
}

