package app.staples.mobile.cfa.profile;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.crittercism.app.Crittercism;
import com.staples.mobile.common.access.easyopen.model.member.Address;
import com.staples.mobile.common.access.easyopen.model.member.Member;

import java.util.List;

import app.staples.R;
import app.staples.mobile.cfa.DrawerItem;
import app.staples.mobile.cfa.MainActivity;
import app.staples.mobile.cfa.widget.ActionBar;

public class AddressListFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = AddressListFragment.class.getSimpleName();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        Crittercism.leaveBreadcrumb("AddressListFragment:onCreateView(): Displaying the Address List screen.");
        View view = inflater.inflate(R.layout.profile_list_fragment, container, false);

        Member member = ProfileDetails.getMember();
        if (member != null) {
            List<Address> addressList = ProfileDetails.getMember().getAddress();
            if (addressList != null) {
                AddressArrayAdapter adapter = new AddressArrayAdapter(getActivity(), addressList, ProfileDetails.currentAddressId);
                ListView listview = (ListView) view.findViewById(R.id.profile_list_view);
                listview.setAdapter(adapter);
//                registerForContextMenu(listview);
            }
        }

        view.findViewById(R.id.listAddButton).setOnClickListener(this);
        return (view);
    }

    @Override
    public void onResume() {
        super.onResume();
        ActionBar.getInstance().setConfig(ActionBar.Config.ADDRESS);
    }

    @Override
    public void onClick(View view) {
        Activity activity = getActivity();
        Fragment shippingFragment = Fragment.instantiate(activity, AddressFragment.class.getName());
        ((MainActivity) activity).selectFragment(DrawerItem.ADDRESS, shippingFragment, MainActivity.Transition.RIGHT);
    }
}
