package com.staples.mobile.cfa.profile;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;

import com.staples.mobile.cfa.MainActivity;
import com.staples.mobile.cfa.R;
import com.staples.mobile.cfa.widget.ActionBar;
import com.staples.mobile.common.access.Access;
import com.staples.mobile.common.access.easyopen.api.EasyOpenApi;
import com.staples.mobile.common.access.easyopen.model.ApiError;
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
public class AddressFragment extends Fragment
    implements View.OnClickListener,
               AdapterView.OnItemClickListener,
               PlacesArrayAdapter.PlaceDataCallback
{
    private static final String TAG = "Add Shipping Fragment";

    private static final String ENTRY_MODE_KEY = "ENTRY_MODE";

    private static final boolean LOGGING = false;

    public enum ENTRY_MODE {
        AUTO_COMPLETE,
        MANUAL,
    }

    Button addShippingBtn;
    public String firstName;
    public String lastName;
    public String addressLine1;
    public String addressLine2;
    public String city;
    public String state;
    public String phoneNumber;
    public String zipCode;
    EasyOpenApi easyOpenApi;
    Activity activity;
    private Resources resources;
    String addressId;

    EditText firstNameET;
    EditText lastNameET;
    EditText addressLineET;
    EditText apartmentET;
    EditText cityET;
    EditText stateET;
    EditText phoneNumberET;
    EditText zipCodeET;

    AutoCompleteTextView addressLineACTV;
    private PlacesArrayAdapter placesArrayAdapter;
    private PlacesArrayAdapter.PlaceData placeData;

    private ENTRY_MODE entryMode;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {

        if (LOGGING) {
            Log.v(TAG, "AddressFragment:onCreateView():"
                            + " bundle[" + bundle + "]"
                            + " this[" + this + "]");
        }

        activity = getActivity();
        resources = activity.getResources();
        easyOpenApi = Access.getInstance().getEasyOpenApi(true);
        View view = inflater.inflate(R.layout.address_fragment, container, false);

        placesArrayAdapter = new PlacesArrayAdapter(activity, R.layout.places_list_item);

        addressLineACTV = (AutoCompleteTextView) view.findViewById(R.id.addressACTV);
        addressLineACTV.setAdapter(placesArrayAdapter);
        addressLineACTV.setOnItemClickListener(this);
        addressLineACTV.setFocusable(true);
        addressLineACTV.setFocusableInTouchMode(true);

        Bundle args = getArguments();
        if(args != null) {
            Address address = (Address)args.getSerializable("addressData");
            if(address != null) {
                entryMode = ENTRY_MODE.MANUAL;
                firstNameET.setText(address.getFirstname());
                lastNameET.setText(address.getLastname());
                addressLineET.setText(address.getAddress1());
                apartmentET.setText(address.getAddress2());
                cityET.setText(address.getCity());
                stateET.setText(address.getState());
                phoneNumberET.setText(address.getPhone1());
                zipCodeET.setText(address.getZipcode());
                addressId = address.getAddressId();
            }
        } else if (bundle != null) {
            entryMode = (ENTRY_MODE) bundle.getSerializable(ENTRY_MODE_KEY);
        } else {
            entryMode = ENTRY_MODE.AUTO_COMPLETE;
        }

        if (entryMode == ENTRY_MODE.AUTO_COMPLETE) {
            setEntryModeAutoComplete();
        } else {
            setEntryModeManual();
        }

        addShippingBtn = (Button) view.findViewById(R.id.address_save);
        addShippingBtn.setOnClickListener(this);

        return (view);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceStateBundle) {
        if (LOGGING) {
            Log.v(TAG, "AddressFragment:onItemClick():"
                            + " savedInstanceStateBundle[" + savedInstanceStateBundle + "]"
                            + " this[" + this + "]");
        }
        savedInstanceStateBundle.putSerializable(ENTRY_MODE_KEY, entryMode);
        super.onSaveInstanceState(savedInstanceStateBundle);
    }

    @Override
    public void onResume() {
        super.onResume();
        ActionBar.getInstance().setConfig(ActionBar.Config.ADDRESS);
    }

    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

        if (LOGGING) Log.v(TAG, "AddressFragment:onItemClick():"
                        + " position[" + position + "]"
                        + " id[" + id + "]"
                        + " this[" + this + "]"
        );

        ((MainActivity)activity).hideSoftKeyboard(addressLineACTV);

        String inputManually = resources.getString(R.string.input_manually_allcaps);
        String resultItem = placesArrayAdapter.getItem(position);

        if (resultItem.equals(inputManually) ) {
            setEntryModeManual();
        } else {
            placesArrayAdapter.getPlaceDetails(position, this);
        }
    }

    public void onPlaceDataResult(PlacesArrayAdapter.PlaceData placeData) {

        // <<<<< Runs on the UI thread. >>>>>

        if (LOGGING) Log.v(TAG, "AddressFragment:onPlaceDataResult():"
                        + " placeData[" + placeData + "]"
                        + " this[" + this + "]"
        );

        this.placeData = placeData;

        String fullZipCode = placeData.getFullZipCode();
        if (!TextUtils.isEmpty(fullZipCode)) {
            addressLineACTV.setText(addressLineACTV.getText().toString() + " " + fullZipCode);
        }

        addressLineACTV.dismissDropDown();
    }

    @Override
    public void onClick(View view) {
        ((MainActivity)activity).hideSoftKeyboard(view);
        ((MainActivity)activity).showProgressIndicator();

        if (entryMode == ENTRY_MODE.AUTO_COMPLETE) {
            doSaveAutoComplete();
        } else {
            doSaveManual();
        }
    }

    private void doSaveManual() {

        if (LOGGING) Log.v(TAG, "AddressFragment:doSaveManual():"
                        + " this[" + this + "]"
        );

        firstName = firstNameET.getText().toString();
        lastName = lastNameET.getText().toString();
        addressLine1 = addressLineET.getText().toString();
        addressLine2 = apartmentET.getText().toString();
        city = cityET.getText().toString();
        state = stateET.getText().toString();
        phoneNumber = phoneNumberET.getText().toString();
        zipCode = zipCodeET.getText().toString();

        if ( ! firstName.isEmpty() && ! lastName.isEmpty() && ! addressLine1.isEmpty() && ! city.isEmpty() &&
                 ! state.isEmpty() && ! phoneNumber.isEmpty() && ! zipCode.isEmpty()) {

            if(addressId != null) {
                UpdateAddress updatedAddress = new UpdateAddress(firstName, lastName, addressLine1, city, state, phoneNumber, zipCode, addressId);
                easyOpenApi.updateMemberAddress(updatedAddress, new Callback<Response>() {
                    @Override
                    public void success(Response response, Response response2) {
                        (new ProfileDetails()).refreshProfile(new ProfileDetails.ProfileRefreshCallback() {
                            @Override public void onProfileRefresh(Member member, String errMsg) {
                                ((MainActivity)activity).hideProgressIndicator();
                                ((MainActivity)activity).showNotificationBanner(R.string.address_updated);
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
                        ((MainActivity)activity).showErrorDialog(ApiError.getErrorMessage(error));
                    }
                });
            }
            else{
                AddAddress address = new AddAddress(firstName, lastName, addressLine1, city, state, phoneNumber, zipCode);
                easyOpenApi.addMemberAddress(address, new Callback<AddressId>() {

                    @Override
                    public void success(AddressId addressId, Response response) {
                        (new ProfileDetails()).refreshProfile(new ProfileDetails.ProfileRefreshCallback() {
                            @Override public void onProfileRefresh(Member member, String errMsg) {
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
                        ((MainActivity)activity).showErrorDialog(ApiError.getErrorMessage(error), false);
                    }
                });
            }
        }
        else {
            ((MainActivity)activity).hideProgressIndicator();
            ((MainActivity)activity).showErrorDialog(R.string.all_fields_required);
        }
    }

    private void doSaveAutoComplete() {

        if (LOGGING) Log.v(TAG, "AddressFragment:doSaveAutoComplete():"
                        + " this[" + this + "]"
        );

        firstName = firstNameET.getText().toString();
        lastName = lastNameET.getText().toString();
        phoneNumber = phoneNumberET.getText().toString();

        addressLine1 = placeData.streetAddress;
        city = placeData.city;
        state = placeData.state;
        zipCode = placeData.zipCode + placeData.zipCodeSuffix;

        if ( ! firstName.isEmpty() && ! lastName.isEmpty() && ! addressLine1.isEmpty() && ! city.isEmpty() &&
                 ! state.isEmpty() && ! phoneNumber.isEmpty() && ! zipCode.isEmpty()) {

            AddAddress address = new AddAddress(firstName, lastName, addressLine1, city, state, phoneNumber, zipCode);
            easyOpenApi.addMemberAddress(address, new Callback<AddressId>() {

                @Override
                public void success(AddressId addressId, Response response) {
                    (new ProfileDetails()).refreshProfile(new ProfileDetails.ProfileRefreshCallback() {
                        @Override public void onProfileRefresh(Member member, String errMsg) {
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
                    ((MainActivity)activity).showErrorDialog(ApiError.getErrorMessage(error), false);
                }
            });
        } else {
            ((MainActivity)activity).hideProgressIndicator();
            ((MainActivity)activity).showErrorDialog(R.string.all_fields_required);
        }
    }

    private void setEntryModeAutoComplete() {

        if (LOGGING) {
            Log.v(TAG, "AddressFragment:setEntryModeAutoComplete():"
                            + " this[" + this + "]");
        }

        entryMode = ENTRY_MODE.AUTO_COMPLETE;

        addressLineACTV.setVisibility(View.VISIBLE);
        addressLineACTV.dismissDropDown();
        addressLineACTV.clearListSelection();
        addressLineET.setVisibility(View.GONE);

        apartmentET.setVisibility(View.GONE);

        cityET.setVisibility(View.GONE);

        stateET.setVisibility(View.GONE);

        zipCodeET.setVisibility(View.GONE);

        firstNameET.requestFocus();
    }

    private void setEntryModeManual() {

        if (LOGGING) {
            Log.v(TAG, "AddressFragment:setEntryModeManual():"
                            + " this[" + this + "]");
        }

        entryMode = ENTRY_MODE.MANUAL;

        addressLineACTV.setVisibility(View.GONE);
        addressLineET.setVisibility(View.VISIBLE);

        apartmentET.setVisibility(View.VISIBLE);

        cityET.setVisibility(View.VISIBLE);

        stateET.setVisibility(View.VISIBLE);

        zipCodeET.setVisibility(View.VISIBLE);

        firstNameET.requestFocus();
    }
}
