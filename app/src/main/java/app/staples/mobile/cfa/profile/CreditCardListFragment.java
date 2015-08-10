package app.staples.mobile.cfa.profile;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.crittercism.app.Crittercism;
import com.staples.mobile.common.access.easyopen.model.member.CCDetails;
import com.staples.mobile.common.access.easyopen.model.member.Member;

import java.util.List;

import app.staples.R;
import app.staples.mobile.cfa.DrawerItem;
import app.staples.mobile.cfa.MainActivity;
import app.staples.mobile.cfa.widget.ActionBar;

public class CreditCardListFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = CreditCardListFragment.class.getSimpleName();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        Crittercism.leaveBreadcrumb("CreditCardListFragment:onCreateView(): Displaying the Credit Card List screen.");
        View view = inflater.inflate(R.layout.profile_list_fragment, container, false);

        Member member = ProfileDetails.getMember();
        if (member != null) {
            List<CCDetails> cardList = member.getCreditCard();
            if (cardList != null) {
                CreditCardArrayAdapter adapter = new CreditCardArrayAdapter(getActivity(), cardList, ProfileDetails.currentPaymentMethodId);
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
        ActionBar.getInstance().setConfig(ActionBar.Config.VIEWCARD);
    }

    @Override
    public void onClick(View view) {
        Activity activity = getActivity();
        Fragment cardFragment = Fragment.instantiate(activity, CreditCardFragment.class.getName());
        ((MainActivity) activity).selectFragment(DrawerItem.CARD, cardFragment, MainActivity.Transition.RIGHT);
    }
}
