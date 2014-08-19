package com.staples.drawertest;

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
public class TopCategoryFragment extends Fragment {
    private static final String TAG = "TopCategoryFragment";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        String title;

        Log.d(TAG, "onCreateView()");
        View view = inflater.inflate(R.layout.topcategory, container, false);
        Bundle args = getArguments();
        Log.d(TAG, "getArguments() "+args);
        if (args!=null) title = args.getString("title");
        else title = "<no arguments>";
        ((TextView) view.findViewById(R.id.title)).setText(title);
        return(view);
    }
}
