package com.staples.mobile.cfa.notify;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.staples.mobile.cfa.R;
import com.staples.mobile.cfa.widget.ActionBar;
import com.urbanairship.UAirship;
import com.urbanairship.push.PushManager;

public class NotifyPrefsFragment extends Fragment implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {
    private static final String TAG = "NotifyPrefsFragment";

    private EditText aliasText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        ViewGroup view = (ViewGroup) inflater.inflate(R.layout.notify_prefs_fragment, container, false);

        PushManager manager = UAirship.shared().getPushManager();
        String alias = manager.getAlias();
        aliasText = (EditText) view.findViewById(R.id.alias_text);
        aliasText.setText(alias);

        view.findViewById(R.id.set_alias).setOnClickListener(this);

        ViewGroup list = (ViewGroup) view.findViewById(R.id.list);
        add_prefs_item(inflater, list, "Orders");
        add_prefs_item(inflater, list, "Daily deals");
        add_prefs_item(inflater, list, "Clearance deals");
        add_prefs_item(inflater, list, "Store openings");

        return(view);
    }

    private View add_prefs_item(LayoutInflater inflater, ViewGroup parent, String title) {
        View item = inflater.inflate(R.layout.notify_prefs_item, parent, false);
        ((TextView) item.findViewById(R.id.title)).setText(title);
        CompoundButton enable = (CompoundButton) item.findViewById(R.id.enable);
        enable.setChecked(true);
        enable.setOnCheckedChangeListener(this);
        parent.addView(item);
        return(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        ActionBar.getInstance().setConfig(ActionBar.Config.NOTIFY);
    }

    @Override
    public void onClick(View view) {
        String alias = aliasText.getText().toString();
        if (!alias.isEmpty()) {
            PushManager manager = UAirship.shared().getPushManager();
            manager.setAlias(alias);
            Toast.makeText(getActivity(), "Alias changed", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton button, boolean isChecked) {
        // TODO
    }
}
