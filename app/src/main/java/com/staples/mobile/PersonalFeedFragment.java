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
public class PersonalFeedFragment extends Fragment {
    private static final String TAG = "SplashFragment";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        Log.d(TAG, "onCreateView()");
        ViewGroup group = (ViewGroup) inflater.inflate(R.layout.personal_feed, container, false);
        inflater.inflate(R.layout.your_store, group);
        inflater.inflate(R.layout.order_track, group);
        inflater.inflate(R.layout.rewards, group);
        return(group);
    }
}
