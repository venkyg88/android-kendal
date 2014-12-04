package com.staples.mobile.cfa.profile;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.staples.mobile.cfa.R;
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
        activity = getActivity();
        addBtn = (Button) view.findViewById(R.id.listAddButton);
        addBtn.setText("Add Credit Card");
        addBtn.setOnClickListener(this);
        cardList = ProfileDetails.getMember().getCreditCard();


        final CreditCardArrayAdapter adapter = new CreditCardArrayAdapter(activity,
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
    public void onResume() {
        super.onResume();
        ((MainActivity)activity).setActionBarTitle(getResources().getString(R.string.credit_card_title));
    }

    @Override
    public void onClick(View view) {
        Fragment cardFragment = Fragment.instantiate(activity, CreditCardFragment.class.getName());
        ((MainActivity) activity).navigateToFragment(cardFragment);
        }
    }

