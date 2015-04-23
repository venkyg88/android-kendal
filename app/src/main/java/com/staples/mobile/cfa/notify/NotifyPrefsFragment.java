package com.staples.mobile.cfa.notify;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.crittercism.app.Crittercism;
import com.staples.mobile.cfa.R;
import com.staples.mobile.cfa.widget.ActionBar;
import com.staples.mobile.common.analytics.Tracker;
import com.urbanairship.UAirship;
import com.urbanairship.push.PushManager;

import java.util.HashSet;
import java.util.Set;

public class NotifyPrefsFragment extends Fragment implements CompoundButton.OnCheckedChangeListener {
    private static final String TAG = NotifyPrefsFragment.class.getSimpleName();

    private EditText aliasText;
    private TagItemAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        Crittercism.leaveBreadcrumb("NotifyPrefsFragment:onCreateView(): Entry.");
        ViewGroup view = (ViewGroup) inflater.inflate(R.layout.notify_prefs_fragment, container, false);

        RecyclerView list = (RecyclerView) view.findViewById(R.id.list);
        adapter = new TagItemAdapter(getActivity());
        list.setAdapter(adapter);
        list.setLayoutManager(new LinearLayoutManager(getActivity()));

        // TODO Hardwired tags should come from MVS
        adapter.addTagItem("promotions", "Promotions");

        getTags();
        adapter.setOnCheckedChangedListener(this);

        return (view);
    }

    @Override
    public void onResume() {
        super.onResume();
        ActionBar.getInstance().setConfig(ActionBar.Config.NOTIFY);
        Tracker.getInstance().trackStateForNotifPreferences(); // analytics
    }

    private void getTags() {
        PushManager manager = UAirship.shared().getPushManager();
        Set<String> set = manager.getTags();
        int n = adapter.getItemCount();
        for(int i = 0; i < n; i++) {
            TagItemAdapter.Item item = adapter.getItem(i);
            item.enable = (set.contains(item.tag));
        }
    }

    private void setTags() {
        HashSet<String> tags = new HashSet<String>();
        int n = adapter.getItemCount();
        for(int i = 0; i < n; i++) {
            TagItemAdapter.Item item = adapter.getItem(i);
            if (item.enable) tags.add(item.tag);
        }
        PushManager manager = UAirship.shared().getPushManager();
        manager.setTags(tags);
    }

    @Override
    public void onCheckedChanged(CompoundButton button, boolean isChecked) {
        Object obj = button.getTag();
        if (obj instanceof TagItemAdapter.Item) {
            TagItemAdapter.Item item = (TagItemAdapter.Item) obj;
            item.enable = isChecked;

            setTags();
        }
    }
}
