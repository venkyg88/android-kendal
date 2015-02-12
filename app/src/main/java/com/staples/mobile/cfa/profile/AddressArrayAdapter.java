package com.staples.mobile.cfa.profile;

import android.app.Fragment;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.staples.mobile.cfa.MainActivity;
import com.staples.mobile.cfa.R;
import com.staples.mobile.common.access.Access;
import com.staples.mobile.common.access.easyopen.api.EasyOpenApi;
import com.staples.mobile.common.access.easyopen.model.ApiError;
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


    private final Context context;
    private final List<Address> values;
    private String selectedAddressId;
    ImageButton optionButton;
    EasyOpenApi easyOpenApi;
    View rowView;

    public AddressArrayAdapter(Context context, List<Address> values, String selectedAddressId) {
        super(context, R.layout.profile_listview_row, values);
        this.context = context;
        this.values = values;
        this.selectedAddressId = selectedAddressId;
        easyOpenApi = Access.getInstance().getEasyOpenApi(true);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowView = inflater.inflate(R.layout.profile_listview_row, parent, false);
        }
        else {
            rowView  = convertView;
        }
        Address address = values.get(position);
// TODO The below code is brittle and dangerous
//        String tmpName = Character.toUpperCase(address.getFirstName().charAt(0)) + address.getFirstName().substring(1) + " " + Character.toUpperCase(address.getLastName().charAt(0)) + address.getLastName().substring(1);
//        String tmpAddress = Character.toUpperCase(address.getAddress1().charAt(0)) +  address.getAddress1().substring(1) + "," + "\n" +
//                Character.toUpperCase(address.getCity().charAt(0)) +  address.getCity().substring(1) + ", " + address.getState().toUpperCase() + " " + address.getZipcode().substring(0,5) + "\n" +
//                address.getPhone1();
        String tmpName = address.getFirstName()+" "+address.getLastName();
        StringBuilder addressBuf = new StringBuilder();
        addressBuf.append(address.getAddress1());
        if (!TextUtils.isEmpty(address.getAddress2())) { // conditionally append line 2 which may be an apt #
            addressBuf.append(" ").append(address.getAddress2());
        }
        addressBuf.append("\n").append(address.getCity()).append(", ").append(address.getState())
                .append(" ").append(address.getZipCode()).append("\n").append(address.getPhone1());

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

        TextView nameText = (TextView) rowView.findViewById(R.id.rowItemText);
        TextView addressText = (TextView) rowView.findViewById(R.id.secondItemText);
        addressText.setText(addressBuf.toString());
        addressText.setTextColor(context.getResources().getColor(R.color.text_black));
        nameText.setText(tmpName);
        nameText.setTypeface(null, Typeface.BOLD);
        nameText.setTag(position);
        nameText.setOnClickListener(this);
        addressText.setTag(position);
        addressText.setOnClickListener(this);

        return rowView;
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.secondItemText:
            case R.id.rowItemText:
                final int position=(Integer)view.getTag();
                String addressId = values.get(position).getAddressId();
                if (ProfileDetails.addressSelectionListener != null) {
                    ProfileDetails.addressSelectionListener.onAddressSelected(addressId);
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
        easyOpenApi.deleteMemberAddress(addressId, new Callback<Response>() {

            @Override
            public void success(Response response, Response response2) {
                (new ProfileDetails()).refreshProfile(new ProfileDetails.ProfileRefreshCallback() {
                    @Override public void onProfileRefresh(Member member, String errMsg) {
                        ((MainActivity)context).hideProgressIndicator();
                        values.remove(position);
                        notifyDataSetChanged();
                        ((MainActivity)context).showNotificationBanner(R.string.address_deleted);
                    }
                });
            }

            @Override
            public void failure(RetrofitError error) {
                ((MainActivity)context).hideProgressIndicator();
                ((MainActivity)context).showErrorDialog(ApiError.getErrorMessage(error));
            }
        });
    }
}


