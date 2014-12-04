package com.staples.mobile.cfa.profile;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.staples.mobile.cfa.R;
import com.staples.mobile.common.access.easyopen.model.member.Address;

import java.util.List;

/**
 * Created by Avinash Dodda.
 */
public class AddressArrayAdapter extends ArrayAdapter<Address> {
    private final Context context;
    private final List<Address> values;
    private String selectedAddressId;

    public AddressArrayAdapter(Context context, List<Address> values, String selectedAddressId) {
        super(context, R.layout.list_view_row, values);
        this.context = context;
        this.values = values;
        this.selectedAddressId = selectedAddressId;
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
        if (selectedAddressId != null) {
            View selectionImageView = rowView.findViewById(R.id.selectionImage);
            if (selectedAddressId.equals(address.getAddressId())) {
                selectionImageView.setVisibility(View.VISIBLE); // visible
            } else {
                selectionImageView.setVisibility(View.INVISIBLE); // invisible but taking up space
            }
        }
        TextView ccText = (TextView) rowView.findViewById(R.id.rowItemText);
        ccText.setText(tmpAddress);
        return rowView;
    }
}


