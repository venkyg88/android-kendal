package com.staples.mobile.cfa.profile;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;

import com.staples.mobile.cfa.MainActivity;
import com.staples.mobile.cfa.R;
import com.staples.mobile.cfa.widget.ActionBar;
import com.staples.mobile.common.access.easyopen.model.member.CCDetails;

import java.util.List;

/**
 * Created by Avinash Dodda.
 */
public class CreditCardListFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = "CreditCardListFragment";
    ListView listview;
    ImageButton addBtn;
    List<CCDetails> cardList;
    Activity activity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        View view = inflater.inflate(R.layout.profile_list_fragment, container, false);
        listview = (ListView) view.findViewById(R.id.profileListView);
        activity = getActivity();
        addBtn = (ImageButton) view.findViewById(R.id.listAddButton);
        addBtn.setOnClickListener(this);
        cardList = ProfileDetails.getMember().getCreditCard();


        final CreditCardArrayAdapter adapter = new CreditCardArrayAdapter(activity,
                cardList, ProfileDetails.currentPaymentMethodId);
        listview.setAdapter(adapter);
        registerForContextMenu(listview);

        return (view);
    }

    @Override
    public void onResume() {
        super.onResume();
        ActionBar.getInstance().setConfig(ActionBar.Config.VIEWCARD);
    }

    @Override
    public void onClick(View view) {
        Fragment cardFragment = Fragment.instantiate(activity, CreditCardFragment.class.getName());
        ((MainActivity) activity).navigateToFragment(cardFragment);
        }
    }

