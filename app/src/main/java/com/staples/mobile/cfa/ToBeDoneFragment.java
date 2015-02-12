package com.staples.mobile.cfa;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.staples.mobile.cfa.widget.ActionBar;

public class ToBeDoneFragment extends Fragment {
    private static final String TAG = "ToBeDoneFragment";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        ViewGroup view = (ViewGroup) inflater.inflate(R.layout.tobedone, container, false);

        Bundle args = getArguments();
        if (args!=null) {
            String title = args.getString("title");
            ((TextView) view.findViewById(R.id.title)).setText(title);
        }

        // TODO Not for release
        for(int size : new int[] {10, 11, 12, 13, 14, 15, 16, 18, 20, 22, 24, 26, 28, 30, 32}) {
            TextView test = new TextView(getActivity());
            test.setTextSize(size);
            test.setText("This is " + size + "sp text.");
            view.addView(test);
        }

        return(view);
     }

    @Override
    public void onResume() {
        super.onResume();
        ActionBar.getInstance().setConfig(ActionBar.Config.DEFAULT);
    }
}
