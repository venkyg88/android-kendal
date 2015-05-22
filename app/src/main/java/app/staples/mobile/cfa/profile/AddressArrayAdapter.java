package app.staples.mobile.cfa.profile;

import android.app.Fragment;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.staples.mobile.common.access.Access;
import com.staples.mobile.common.access.easyopen.api.EasyOpenApi;
import com.staples.mobile.common.access.easyopen.model.ApiError;
import com.staples.mobile.common.access.easyopen.model.EmptyResponse;
import com.staples.mobile.common.access.easyopen.model.member.Address;
import com.staples.mobile.common.access.easyopen.model.member.Member;

import java.util.List;

import app.staples.R;
import app.staples.mobile.cfa.DrawerItem;
import app.staples.mobile.cfa.MainActivity;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class AddressArrayAdapter extends ArrayAdapter<Address> implements View.OnClickListener{

    private Context context;
    private String selectedAddressId;
    private LayoutInflater inflater;

    public AddressArrayAdapter(Context context, List<Address> values, String selectedAddressId) {
        super(context, R.layout.profile_listview_row);
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.context = context;
        this.selectedAddressId = selectedAddressId;

        if (values != null) {
            addAll(values);
        }
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        if(view == null) {
            view = inflater.inflate(R.layout.profile_listview_row, parent, false);
        }
        Address address = getItem(position);

        String tmpName = address.getFirstName() + " " + address.getLastName();

        String formattedPhoneNumber;
        if(address.getPhone1() != null && address.getPhone1().length() == 10){
            formattedPhoneNumber = PhoneNumberUtils.formatNumber(address.getPhone1());
        } else {
            formattedPhoneNumber = address.getPhone1();
        }

        StringBuilder addressBuf = new StringBuilder();
        addressBuf.append(address.getAddress1());
        if (!TextUtils.isEmpty(address.getAddress2())) { // conditionally append line 2 which may be an apt #
            addressBuf.append(" ").append(address.getAddress2());
        }

        addressBuf.append("\n").append(address.getCity()).append(", ").append(address.getState())
                .append(" ").append(address.getZipCode()).append("\n").append(formattedPhoneNumber);

        ImageButton optionButton = (ImageButton) view.findViewById(R.id.listOptions);
        optionButton.setTag(address);
        optionButton.setOnClickListener(this);

        if (selectedAddressId != null) {
            View selectionImageView = view.findViewById(R.id.selectionImage);
            if (selectedAddressId.equals(address.getAddressId())) {
                selectionImageView.setVisibility(View.VISIBLE); // visible
            } else {
                selectionImageView.setVisibility(View.INVISIBLE); // invisible but taking up space
            }
        }

        TextView nameText = (TextView) view.findViewById(R.id.rowItemText);
        TextView addressText = (TextView) view.findViewById(R.id.secondItemText);
        addressText.setText(addressBuf.toString());
        addressText.setTextColor(context.getResources().getColor(R.color.staples_black));
        nameText.setText(tmpName);
        nameText.setTypeface(null, Typeface.BOLD);
        View itemLayout = view.findViewById(R.id.item_layout);
        itemLayout.setOnClickListener(this);
        itemLayout.setTag(address);

        return view;
    }

    @Override
    public void onClick(View view) {
        final Address address;
        switch(view.getId()){
            case R.id.item_layout:
                address = (Address) view.getTag();
                if(address != null) {
                    String addressId = address.getAddressId();
                    if (ProfileDetails.addressSelectionListener != null) {
                        ProfileDetails.addressSelectionListener.onAddressSelected(addressId);
                    }
                }
                break;

            case R.id.listOptions:
                address = (Address) view.getTag();
                if(address != null) {
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
                                    updateAddress(address);
                                    return true;
                                case R.id.deleteListItem:
                                    deleteAddress(address);
                                    return true;
                                default:
                                    return true;
                            }
                        }
                    });
                    popup.show(); //showing popup menu
                }
                break;
        }
    }

    public void updateAddress(final Address address){
        Fragment addressFragment = Fragment.instantiate(context, AddressFragment.class.getName());
        Bundle bundle = new Bundle();
        bundle.putSerializable("addressData", address);
        addressFragment.setArguments(bundle);
        ((MainActivity)context).selectFragment(DrawerItem.ADDRESS, addressFragment, MainActivity.Transition.RIGHT);
    }

    public void deleteAddress(final Address address){
        String addressId = address.getAddressId();
        ((MainActivity)context).showProgressIndicator();
        EasyOpenApi easyOpenApi = Access.getInstance().getEasyOpenApi(true);
        easyOpenApi.deleteMemberAddress(addressId, new Callback<EmptyResponse>() {

            @Override
            public void success(EmptyResponse empty, Response response) {
                (new ProfileDetails()).refreshProfile(new ProfileDetails.ProfileRefreshCallback() {
                    @Override public void onProfileRefresh(Member member, String errMsg) {
                        ((MainActivity)context).hideProgressIndicator();
                        remove(address);
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

