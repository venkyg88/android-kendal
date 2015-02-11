package com.staples.mobile.cfa.profile;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.staples.mobile.cfa.MainActivity;
import com.staples.mobile.cfa.R;
import com.staples.mobile.cfa.widget.ActionBar;
import com.staples.mobile.cfa.widget.AddressBlock;
import com.staples.mobile.common.access.Access;
import com.staples.mobile.common.access.easyopen.api.EasyOpenApi;
import com.staples.mobile.common.access.easyopen.model.ApiError;
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
public class AddressFragment extends Fragment implements Callback<AddressId>, View.OnClickListener
{
    private static final String TAG = "AddressFragment";

    private AddressBlock addressBlock;
    private String addressId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        View view = inflater.inflate(R.layout.address_fragment, container, false);
        addressBlock = (AddressBlock) view.findViewById(R.id.shipping_address);

        boolean autoMode = true;
        Bundle args = getArguments();
        if (args!=null) {
            Address address = (Address) args.getSerializable("addressData");
            if (address != null) {
                addressBlock.setAddress(address);
                addressId = address.getAddressId();
                String line1 = address.getAddress1();
                if (line1!=null && line1.length()>0) autoMode = false;
            }
        }
        addressBlock.init(autoMode, false);

        view.findViewById(R.id.address_save).setOnClickListener(this);
        return (view);
    }

    @Override
    public void onResume() {
        super.onResume();
        ActionBar.getInstance().setConfig(ActionBar.Config.ADDRESS);
    }

    private void save() {
        MainActivity activity = (MainActivity) getActivity();
        activity.showProgressIndicator();

        EasyOpenApi easyOpenApi = Access.getInstance().getEasyOpenApi(true);
        UpdateAddress addr = addressBlock.getUpdateAddress();
        if (addressId!=null) {
            addr.setAddressId(addressId);
            easyOpenApi.updateMemberAddress(addr, this);
        } else {
            easyOpenApi.addMemberAddress(addr, this);
        }
    }

    @Override
    public void success(AddressId addressId, Response response) {
        (new ProfileDetails()).refreshProfile(new ProfileDetails.ProfileRefreshCallback() {
            @Override public void onProfileRefresh(Member member, String errMsg) {
                MainActivity activity = (MainActivity) getActivity();
                activity.hideProgressIndicator();
                activity.showNotificationBanner(R.string.address_updated);
                FragmentManager fm = getFragmentManager();
                if (fm != null) {
                    fm.popBackStack(); // this will take us back to one of the many places that could have opened this page
                }
            }
        });
    }

    @Override
    public void failure(RetrofitError error) {
        MainActivity activity = (MainActivity) getActivity();
        activity.hideProgressIndicator();
        activity.showErrorDialog(ApiError.getErrorMessage(error));
    }

    @Override
    public void onClick(View view) {
        save();
    }
}
