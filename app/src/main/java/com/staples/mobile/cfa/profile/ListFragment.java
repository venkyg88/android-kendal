package com.staples.mobile.cfa.profile;

import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.staples.mobile.R;
import com.staples.mobile.cfa.BaseFragment;
import com.staples.mobile.cfa.MainActivity;

import java.util.ArrayList;

/**
 * Created by Avinash Dodda.
 */
public class ListFragment extends BaseFragment implements View.OnClickListener {
    private static final String TAG = "ListFragment";
    ListView listview;
    private ArrayAdapter<String> listAdapter ;
    Button addBtn;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        Log.d(TAG, "onCreateView()");
        View view = inflater.inflate(R.layout.list_fragment, container, false);
        listview = (ListView)view.findViewById(R.id.profileListView);
        addBtn  = (Button)view.findViewById(R.id.listAddButton);
        addBtn.setOnClickListener(this);

        ArrayList<String> list = new ArrayList<String>();
        Bundle args = getArguments();
        if(args != null){
            if(args.getStringArrayList("addresses")!=null){
                list.addAll(args.getStringArrayList("addresses"));
                addBtn.setText("Add Address");
            }
            if(args.getStringArrayList("cards")!=null) {
                list.addAll(args.getStringArrayList("cards"));
                addBtn.setText("Add Card");
            }
        }

        listAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, list);
        listview.setAdapter( listAdapter );

        return(view);
    }

    @Override
    public void onClick(View view) {
        Button btn = (Button)view;
        String buttonText = btn.getText().toString();

        if(buttonText.equals("Add Address")) {
            Fragment shippingFragment = Fragment.instantiate(getActivity(), ShippingFragment.class.getName());
            ((MainActivity) getActivity()).navigateToFragment(shippingFragment);
        }
        else{
            Fragment cardFragment = Fragment.instantiate(getActivity(), CreditCardFragment.class.getName());
            ((MainActivity) getActivity()).navigateToFragment(cardFragment);
        }
    }
}
