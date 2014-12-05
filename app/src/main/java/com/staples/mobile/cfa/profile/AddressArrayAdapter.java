package com.staples.mobile.cfa.profile;

import android.app.Fragment;
import android.app.FragmentManager;
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
import com.staples.mobile.common.access.easyopen.model.member.Address;
import com.staples.mobile.common.access.easyopen.model.member.Member;

import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by Avinash Dodda.
 */
public class AddressArrayAdapter extends ArrayAdapter<Address> implements View.OnClickListener{

    private static final String RECOMMENDATION = "v1";
    private static final String STORE_ID = "10001";
    private static final String CLIENT_ID = LoginHelper.CLIENT_ID;
    private static final String LOCALE = "en_US";

    private final Context context;
    private final List<Address> values;
    private String selectedAddressId;
    ImageButton optionButton;
    EasyOpenApi easyOpenApi;

    public AddressArrayAdapter(Context context, List<Address> values, String selectedAddressId) {
        super(context, R.layout.list_view_row, values);
        this.context = context;
        this.values = values;
        this.selectedAddressId = selectedAddressId;
        easyOpenApi = Access.getInstance().getEasyOpenApi(true);
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

        optionButton = (ImageButton) rowView.findViewById(R.id.listOptions);
        optionButton.setTag(position);
        optionButton.setOnClickListener(this);

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
        ccText.setTag(position);
        ccText.setOnClickListener(this);

        return rowView;
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.rowItemText:
                final int position=(Integer)view.getTag();
                String addressId = values.get(position).getAddressId();
                if (ProfileDetails.addressSelectionListener != null) {
                    ProfileDetails.addressSelectionListener.onAddressSelected(addressId);
                } else {
                    Toast.makeText(context, addressId, Toast.LENGTH_LONG).show();
                }
                break;

            case R.id.listOptions:
                final int position1=(Integer)view.getTag();
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
                                updateAddress(position1);
                                return true;
                            case R.id.deleteListItem:
                                deleteAddress(position1);
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

    public void updateAddress(final int position){
        Fragment addressFragment = Fragment.instantiate(context, AddressFragment.class.getName());
        Bundle bundle = new Bundle();
        bundle.putSerializable("addressData", values.get(position));
        addressFragment.setArguments(bundle);
        ((MainActivity)context).navigateToFragment(addressFragment);
    }

    public void deleteAddress(final int position){
        String addressId = values.get(position).getAddressId();
        ((MainActivity)context).showProgressIndicator();
        easyOpenApi.deleteMemberAddress(RECOMMENDATION, STORE_ID, addressId, CLIENT_ID, LOCALE, new Callback<Response>() {

            @Override
            public void success(Response response, Response response2) {
                (new ProfileDetails()).refreshProfile(new ProfileDetails.ProfileRefreshCallback() {
                    @Override public void onProfileRefresh(Member member) {
                        ((MainActivity)context).hideProgressIndicator();
                        values.remove(position);
                        notifyDataSetChanged();
                        Toast.makeText(context, "Address deleted", Toast.LENGTH_SHORT).show();
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


