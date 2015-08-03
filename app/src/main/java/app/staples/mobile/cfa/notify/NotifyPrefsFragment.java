package app.staples.mobile.cfa.notify;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;

import com.crittercism.app.Crittercism;
import com.staples.mobile.common.analytics.Tracker;

import java.util.HashSet;
import java.util.Set;

import app.staples.R;
import app.staples.mobile.cfa.widget.ActionBar;

public class NotifyPrefsFragment extends Fragment implements CompoundButton.OnCheckedChangeListener {
    private static final String TAG = NotifyPrefsFragment.class.getSimpleName();

    private EditText aliasText;
    private TagItemAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        Crittercism.leaveBreadcrumb("NotifyPrefsFragment:onCreateView(): Displaying the Notification Preferences screen.");
        ViewGroup view = (ViewGroup) inflater.inflate(R.layout.notify_prefs_fragment, container, false);

        RecyclerView list = (RecyclerView) view.findViewById(R.id.list);
        adapter = new TagItemAdapter(getActivity());
        list.setAdapter(adapter);
        list.setLayoutManager(new LinearLayoutManager(getActivity()));

        // TODO Hardwired tags should come from MVS
        adapter.addTagItem("promotions", "Promotions");

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
        if (obj instanceof TagItemAdapter.Item) {
            TagItemAdapter.Item item = (TagItemAdapter.Item) obj;
            item.enable = isChecked;
        }
    }
}
