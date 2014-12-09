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

import com.staples.mobile.cfa.R;
import com.staples.mobile.cfa.BaseFragment;
import com.staples.mobile.cfa.MainActivity;
import com.staples.mobile.common.access.easyopen.model.member.Address;

import java.util.List;

/**
 * Created by Avinash Dodda.
 */
public class AddressListFragment extends BaseFragment implements View.OnClickListener {
    private static final String TAG = "Address List Fragment";
    ListView listview;
    Button addBtn;
    List<Address> addressList;
    Activity activity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        Log.d(TAG, "onCreateView()");
        activity = getActivity();

        View view = inflater.inflate(R.layout.list_fragment, container, false);

        listview = (ListView) view.findViewById(R.id.profileListView);
        addBtn = (Button) view.findViewById(R.id.listAddButton);
        addBtn.setText("Add Address");
        addBtn.setOnClickListener(this);
        addressList = ProfileDetails.getMember().getAddress();

        final AddressArrayAdapter adapter = new AddressArrayAdapter(activity,
                addressList, ProfileDetails.currentAddressId);
        listview.setAdapter(adapter);
        registerForContextMenu(listview);

        return (view);
    }

    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity) activity).showActionBar(R.string.address_title, 0, null);
    }

    @Override
    public void onClick(View view) {
        Fragment shippingFragment = Fragment.instantiate(activity, AddressFragment.class.getName());
        ((MainActivity) activity).navigateToFragment(shippingFragment);
    }
}
