package com.staples.mobile.cfa.profile;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.staples.mobile.cfa.MainActivity;
import com.staples.mobile.cfa.R;
import com.staples.mobile.cfa.login.LoginHelper;
import com.staples.mobile.cfa.widget.ActionBar;
import com.staples.mobile.common.access.Access;
import com.staples.mobile.common.access.easyopen.api.EasyOpenApi;
import com.staples.mobile.common.access.easyopen.model.member.AddAddress;
import com.staples.mobile.common.access.easyopen.model.member.Address;
import com.staples.mobile.common.access.easyopen.model.member.AddressId;
import com.staples.mobile.common.access.easyopen.model.member.Member;
import com.staples.mobile.common.access.easyopen.model.member.UpdateAddress;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by Avinash Raja Dodda.
 */
public class AddressFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "Add Shipping Fragment";

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
    String addressId;

    EditText firstNameET;
    EditText lastNameET;
    EditText addressLineET;
    EditText cityET;
    EditText stateET;
    EditText phoneNumberET;
    EditText zipCodeET;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        Log.d(TAG, "onCreateView()");
        activity = getActivity();
        easyOpenApi = Access.getInstance().getEasyOpenApi(true);
        View view = inflater.inflate(R.layout.add_address_fragment, container, false);

        firstNameET = (EditText) view.findViewById(R.id.firstName);
        lastNameET = (EditText) view.findViewById(R.id.lastName);
        addressLineET = (EditText) view.findViewById(R.id.address);
        cityET = (EditText) view.findViewById(R.id.city);
        stateET = (EditText) view.findViewById(R.id.state);
        phoneNumberET = (EditText) view.findViewById(R.id.phoneNumber);
        zipCodeET = (EditText) view.findViewById(R.id.zipCode);

        Bundle args = getArguments();
        if(args != null) {
            Address address = (Address)args.getSerializable("addressData");
            if(address != null) {
                firstNameET.setText(address.getFirstname());
                lastNameET.setText(address.getLastname());
                addressLineET.setText(address.getAddress1());
                cityET.setText(address.getCity());
                stateET.setText(address.getState());
                phoneNumberET.setText(address.getPhone1());
                zipCodeET.setText(address.getZipcode());
                addressId = address.getAddressId();
            }
        }

        addShippingBtn = (Button) view.findViewById(R.id.addressSaveBtn);
        addShippingBtn.setOnClickListener(this);

        return (view);
    }

    @Override
    public void onResume() {
        super.onResume();
        ActionBar.getInstance().setConfig(ActionBar.Config.ADDRESS);
    }

    public void hideKeyboard(View view)
    {
        InputMethodManager keyboard = (InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        keyboard.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    @Override
    public void onClick(View view) {
        hideKeyboard(view);
        ((MainActivity)activity).showProgressIndicator();
        firstName = firstNameET.getText().toString();
        lastName = lastNameET.getText().toString();
        addressLine1 = addressLineET.getText().toString();
        city = cityET.getText().toString();
        state = stateET.getText().toString();
        phoneNumber = phoneNumberET.getText().toString();
        zipCode = zipCodeET.getText().toString();

        if(!firstName.isEmpty() && !lastName.isEmpty() && !addressLine1.isEmpty() && !city.isEmpty() &&
                !state.isEmpty() && !phoneNumber.isEmpty() && !zipCode.isEmpty()) {
            if(addressId != null) {
                UpdateAddress updatedAddress = new UpdateAddress(firstName, lastName, addressLine1, city, state, phoneNumber, zipCode, addressId);
                easyOpenApi.updateMemberAddress(updatedAddress, new Callback<Response>() {
                    @Override
                    public void success(Response response, Response response2) {
                        (new ProfileDetails()).refreshProfile(new ProfileDetails.ProfileRefreshCallback() {
                            @Override public void onProfileRefresh(Member member) {
                                ((MainActivity)activity).hideProgressIndicator();
                                Toast.makeText(getActivity(), "Address Updated", Toast.LENGTH_LONG).show();
                                FragmentManager fm = getFragmentManager();
                                if (fm != null) {
                                    fm.popBackStack(); // this will take us back to one of the many places that could have opened this page
                                }
                            }
                        });
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        ((MainActivity)activity).hideProgressIndicator();
                        Toast.makeText(getActivity(), error.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            }
            else{
                AddAddress address = new AddAddress(firstName, lastName, addressLine1, city, state, phoneNumber, zipCode);
                easyOpenApi.addMemberAddress(address, new Callback<AddressId>() {

                    @Override
                    public void success(AddressId addressId, Response response) {
                        Toast.makeText(getActivity(), "Address Id " + addressId.getAddressId(), Toast.LENGTH_LONG).show();

                        (new ProfileDetails()).refreshProfile(new ProfileDetails.ProfileRefreshCallback() {
                            @Override public void onProfileRefresh(Member member) {
                                ((MainActivity)activity).hideProgressIndicator();

                                FragmentManager fm = getFragmentManager();
                                if (fm != null) {
                                    fm.popBackStack(); // this will take us back to one of the many places that could have opened this page
                                }
                            }
                        });
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        ((MainActivity)activity).hideProgressIndicator();
                        Toast.makeText(getActivity(), "Error Message " + error.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            }
        }
        else {
            ((MainActivity)activity).hideProgressIndicator();
            Toast.makeText(getActivity(), "All the fields are required. Please fill and try again", Toast.LENGTH_LONG).show();
        }
    }
}
