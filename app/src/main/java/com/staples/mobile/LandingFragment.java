package com.staples.mobile;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by PyhRe001 on 8/11/14.
 */
public class LandingFragment extends Fragment {
    private static final String TAG = "LandingFragment";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        Log.d(TAG, "onCreateView()");
        View view = inflater.inflate(R.layout.landing, container, false);
        return(view);
    }
}
