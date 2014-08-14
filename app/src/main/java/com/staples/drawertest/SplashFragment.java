package com.staples.drawertest;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by PyhRe001 on 8/11/14.
 */
public class SplashFragment extends Fragment {
    private static final String TAG = "SplashFragment";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        Log.d(TAG, "onCreateView()");
        View view = inflater.inflate(R.layout.splash, container, false);
        return(view);
    }
}
