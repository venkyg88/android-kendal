package app.staples.mobile.cfa.profile;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.crittercism.app.Crittercism;
import com.staples.mobile.common.access.Access;
import com.staples.mobile.common.access.easyopen.api.EasyOpenApi;
import com.staples.mobile.common.access.easyopen.model.ApiError;
import com.staples.mobile.common.access.easyopen.model.member.Address;
import com.staples.mobile.common.access.easyopen.model.member.AddressId;
import com.staples.mobile.common.access.easyopen.model.member.Member;
import com.staples.mobile.common.access.easyopen.model.member.UpdateAddress;

import app.staples.R;
import app.staples.mobile.cfa.MainActivity;
import app.staples.mobile.cfa.widget.ActionBar;
import app.staples.mobile.cfa.widget.AddressBlock;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class AddressFragment extends Fragment implements Callback<AddressId>, ProfileDetails.ProfileRefreshCallback, View.OnClickListener
{
    private static final String TAG = AddressFragment.class.getSimpleName();

    private AddressBlock addressBlock;
    private String addressId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        Crittercism.leaveBreadcrumb("AddressFragment:onCreateView(): Displaying the Address screen.");
        View view = inflater.inflate(R.layout.address_fragment, container, false);
        addressBlock = (AddressBlock) view.findViewById(R.id.shipping_address);

        Bundle args = getArguments();
        Address address = null;
        if (args!=null) {
            address = (Address) args.getSerializable("addressData");
            if (address != null) {
                addressBlock.setAddress(address);
                addressId = address.getAddressId();
            }
        }

        addressBlock.init(false);
        addressBlock.selectMode(address == null);

        view.findViewById(R.id.address_save).setOnClickListener(this);
        view.findViewById(R.id.address_cancel).setOnClickListener(this);
        return (view);
    }

    @Override
    public void onResume() {
        super.onResume();
        ActionBar.getInstance().setConfig(ActionBar.Config.ADDRESS);
    }

    private void save(View view) {
        MainActivity activity = (MainActivity) getActivity();
        activity.hideSoftKeyboard();

        switch(view.getId()) {
            case R.id.address_save:
                if (addressBlock.validateBillingAddress()) {

                activity.showProgressIndicator();
                EasyOpenApi easyOpenApi = Access.getInstance().getEasyOpenApi(true);
                UpdateAddress addr = addressBlock.getUpdateAddress();
                if (addressId != null) {
                    addr.setAddressId(addressId);
                    easyOpenApi.updateMemberAddress(addr, this);
                    break;
                } else {
                    easyOpenApi.addMemberAddress(addr, this);
                    break;
                }
                } else {
                    activity.showErrorDialog(R.string.address_error_msg);
                    addressBlock.selectMode(false);
                    break;
                }
            case R.id.address_cancel:
                activity.popBackStack();
                break;
        }

    }

    @Override
    public void success(AddressId addressId, Response response) {
        (new ProfileDetails()).refreshProfile(this);
    }

    @Override
    public void failure(RetrofitError error) {
        MainActivity activity = (MainActivity) getActivity();
        if (activity==null) return;
        activity.hideProgressIndicator();
        activity.showErrorDialog(ApiError.getErrorMessage(error));
    }

    @Override
    public void onProfileRefresh(Member member, String errMsg) {
        MainActivity activity = (MainActivity) getActivity();
        if (activity==null) return;
        activity.hideProgressIndicator();
        activity.showNotificationBanner(R.string.address_updated);
        activity.popBackStack();
    }

    @Override
    public void onClick(View view) {
        save(view);
    }
}
