package com.staples.mobile;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by PyhRe001 on 8/11/14.
 */
public class ToBeDoneFragment extends Fragment {
    private static final String TAG = "ToBeDoneFragment";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        Log.d(TAG, "onCreateView()");
        View view = inflater.inflate(R.layout.tobedone, container, false);
        Bundle args = getArguments();
        String title = args.getString("title");
        ((TextView) view.findViewById(R.id.title)).setText(title);
        return(view);
    }
}
