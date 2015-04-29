package com.staples.mobile.cfa.profile;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ListView;

import com.crittercism.app.Crittercism;
import com.staples.mobile.cfa.DrawerItem;
import com.staples.mobile.cfa.MainActivity;
import com.staples.mobile.cfa.R;
import com.staples.mobile.cfa.widget.ActionBar;
import com.staples.mobile.common.access.easyopen.model.member.Address;

import java.util.List;

public class AddressListFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = "Address List Fragment";
    ListView listview;
    ImageButton addButton;
    List<Address> addressList;
    Activity activity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        Crittercism.leaveBreadcrumb("AddressListFragment:onCreateView(): Displaying the Address List screen.");
        activity = getActivity();

        View view = inflater.inflate(R.layout.profile_list_fragment, container, false);
        listview = (ListView) view.findViewById(R.id.profile_list_view);
        listview.setDivider(null);
        listview.setDividerHeight(0);
        addButton =(ImageButton) view.findViewById(R.id.listAddButton);
        addButton.setOnClickListener(this);
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
        ActionBar.getInstance().setConfig(ActionBar.Config.ADDRESS);
    }

    @Override
    public void onClick(View view) {
        Fragment shippingFragment = Fragment.instantiate(activity, AddressFragment.class.getName());
        ((MainActivity) activity).selectFragment(DrawerItem.ADDRESS, shippingFragment, MainActivity.Transition.RIGHT, true);
    }
}
