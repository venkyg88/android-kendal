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
public class ShippingListFragment extends BaseFragment implements View.OnClickListener {
    private static final String TAG = "ShippingList Fragment";
    ListView listview;
    Button addBtn;
    List<Address> addressList;
    Activity activity;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        Log.d(TAG, "onCreateView()");

        View view = inflater.inflate(R.layout.list_fragment, container, false);
        listview = (ListView) view.findViewById(R.id.profileListView);
        addBtn = (Button) view.findViewById(R.id.listAddButton);
        addBtn.setText("Add Address");
        addBtn.setOnClickListener(this);
        addressList = ProfileDetails.getMember().getAddress();
        activity = getActivity();


        final ShippingArrayAdapter adapter = new ShippingArrayAdapter(activity,
                addressList, ProfileDetails.currentAddressId);
        listview.setAdapter(adapter);
        registerForContextMenu(listview);

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            public void onItemClick(AdapterView<?> a, View v, int position,
                                    long id) {
                String addressId = addressList.get(position).getAddressId();
                if (ProfileDetails.addressSelectionListener != null) {
                    ProfileDetails.addressSelectionListener.onAddressSelected(addressId);
                } else {
                    Toast.makeText(activity, addressId, Toast.LENGTH_LONG).show();
                }
            }
        });
        return (view);
    }

    @Override
    public void onClick(View view) {
        Fragment shippingFragment = Fragment.instantiate(activity, ShippingFragment.class.getName());
        ((MainActivity) activity).navigateToFragment(shippingFragment);
    }
}

class ShippingArrayAdapter extends ArrayAdapter<Address> {
    private final Context context;
    private final List<Address> values;
    private String addressId;

    public ShippingArrayAdapter(Context context, List<Address> values, String addressId) {
        super(context, R.layout.list_view_row, values);
        this.context = context;
        this.values = values;
        this.addressId = addressId;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.list_view_row, parent, false);

        Address address = values.get(position);
        String tmpAddress = address.getFirstname() + ", " + address.getLastname() +  "\n" +
                address.getAddress1() + "\n" +
                address.getCity() + "\n" +
                address.getState() + "," + address.getZipcode() + "\n" +
                address.getPhone1();
        if (addressId != null) {
            View selectionImageView = rowView.findViewById(R.id.selectionImage);
            if (addressId.equals(address.getAddressId())) {
                selectionImageView.setVisibility(View.VISIBLE);
            } else {
                selectionImageView.setVisibility(View.INVISIBLE);
            }
        }
        TextView ccText = (TextView) rowView.findViewById(R.id.rowItemText);
        ccText.setText(tmpAddress);
        return rowView;
    }
}

