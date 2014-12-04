package com.staples.mobile.cfa.profile;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.staples.mobile.cfa.R;
import com.staples.mobile.cfa.BaseFragment;
import com.staples.mobile.cfa.MainActivity;
import com.staples.mobile.cfa.login.LoginHelper;
import com.staples.mobile.common.access.Access;
import com.staples.mobile.common.access.easyopen.api.EasyOpenApi;
import com.staples.mobile.common.access.easyopen.model.member.AddAddress;
import com.staples.mobile.common.access.easyopen.model.member.AddressId;
import com.staples.mobile.common.access.easyopen.model.member.Member;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by Avinash Raja Dodda.
 */
public class AddressFragment extends BaseFragment implements View.OnClickListener {

    private static final String TAG = "Add Shipping Fragment";
    private static final String RECOMMENDATION = "v1";
    private static final String STORE_ID = "10001";
    private static final String CLIENT_ID = LoginHelper.CLIENT_ID;
    private static final String LOCALE = "en_US";

    Button addShippingBtn;
    public String firstName;
    public String lastName;
    public String addressLine1;
    public String city;
    public String state;
    public String phoneNumber;
    public String zipCode;
    EasyOpenApi easyOpenApi;
    Activity activity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {

        Log.d(TAG, "onCreateView()");
        View view = inflater.inflate(R.layout.addshipping_fragment, container, false);

        activity = getActivity();
        easyOpenApi = Access.getInstance().getEasyOpenApi(true);
        addShippingBtn = (Button) view.findViewById(R.id.addressSaveBtn);
        addShippingBtn.setOnClickListener(this);

        return (view);
    }
    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity)activity).setActionBarTitle(getResources().getString(R.string.add_address_title));
    }
    @Override
    public void onClick(View view) {
        firstName = ((EditText) getView().findViewById(R.id.firstName)).getText().toString();
        lastName = ((EditText) getView().findViewById(R.id.lastName)).getText().toString();
        addressLine1 = ((EditText) getView().findViewById(R.id.address)).getText().toString();
        city = ((EditText) getView().findViewById(R.id.city)).getText().toString();
        state = ((EditText) getView().findViewById(R.id.state)).getText().toString();
        phoneNumber = ((EditText) getView().findViewById(R.id.phoneNumber)).getText().toString();
        zipCode = ((EditText) getView().findViewById(R.id.zipCode)).getText().toString();

        if(!firstName.isEmpty() && !lastName.isEmpty() && !addressLine1.isEmpty() && !city.isEmpty() &&
                !state.isEmpty() && !phoneNumber.isEmpty() && !zipCode.isEmpty()) {
            AddAddress address = new AddAddress(firstName, lastName, addressLine1, city, state, phoneNumber, zipCode);
            easyOpenApi.addMemberAddress(address,RECOMMENDATION, STORE_ID, LOCALE, CLIENT_ID, new Callback<AddressId>() {
                @Override
                public void success(AddressId addressId, Response response) {
                    Toast.makeText(getActivity(), "Address Id " + addressId.getAddressId(), Toast.LENGTH_LONG).show();
                    (new ProfileDetails()).refreshProfile(new ProfileDetails.ProfileRefreshCallback() {
                        @Override public void onProfileRefresh(Member member) {
                            FragmentManager fm = getFragmentManager();
                            if (fm != null) {
                                fm.popBackStack(); // this will take us back to one of the many places that could have opened this page
                            }
                        }
                    });
                }

                @Override
                public void failure(RetrofitError error) {
                    Toast.makeText(getActivity(), "Error Message " + error.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        }
        else {
            Toast.makeText(getActivity(), "All the fields are required. Please fill and try again", Toast.LENGTH_LONG).show();
        }
    }

}
