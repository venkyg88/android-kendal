package app.staples.mobile.cfa.notify;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import com.crittercism.app.Crittercism;
import com.leanplum.Leanplum;
import com.staples.mobile.common.analytics.Tracker;

import app.staples.R;
import app.staples.mobile.cfa.widget.ActionBar;

public class NotifyPrefsFragment extends Fragment implements CompoundButton.OnCheckedChangeListener {
    private static final String TAG = NotifyPrefsFragment.class.getSimpleName();

    private NotifyPrefsAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        Crittercism.leaveBreadcrumb("NotifyPrefsFragment:onCreateView(): Displaying the Notification Preferences screen.");
        ViewGroup view = (ViewGroup) inflater.inflate(R.layout.notify_prefs_fragment, container, false);

        RecyclerView list = (RecyclerView) view.findViewById(R.id.list);
        adapter = new NotifyPrefsAdapter(getActivity());
        list.setAdapter(adapter);
        list.setLayoutManager(new LinearLayoutManager(getActivity()));

        // TODO This should come from the MCS and it should be in MainActivity
        NotifyPreferences prefs = NotifyPreferences.getInstance();
        if (prefs.getArray().size()==0) {
            prefs.addItem("marketing", R.string.notify_marketing);
            prefs.addItem("orders", R.string.notify_orders);
            prefs.addItem("rewards", R.string.notify_rewards);
            prefs.loadPreferences(getActivity());
        }

        adapter.setArray(prefs.getArray());
        adapter.setOnCheckedChangedListener(this);

        return (view);
    }

    @Override
    public void onResume() {
        super.onResume();
        ActionBar.getInstance().setConfig(ActionBar.Config.NOTIFY);
        Tracker.getInstance().trackStateForNotifPreferences(); // analytics
    }

    @Override
    public void onCheckedChanged(CompoundButton button, boolean isChecked) {
        Object obj = button.getTag();
        if (obj instanceof NotifyPreferences.Item) {
            NotifyPreferences.Item item = (NotifyPreferences.Item) obj;
            item.enable = isChecked;

            Activity activity = getActivity();
            NotifyPreferences prefs = NotifyPreferences.getInstance();
            prefs.savePreferences(activity); // TODO Should be in MainActivity
            prefs.uploadPreferences(activity);
            Leanplum.setUserAttributes(prefs.getUserAttributes());
        }
    }
}
