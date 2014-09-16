package com.staples.mobile;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by PyhRe001 on 8/11/14.
 */
public class LandingFragment extends Fragment {
    private static final String TAG = "LandingFragment";

    private ViewPager pager;
    private PagerAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        Log.d(TAG, "onCreateView()");
        View view = inflater.inflate(R.layout.landing, container, false);

        FragmentManager manager = getFragmentManager();
//        FragmentManager manager = getChildFragmentManager(); TODO requires API 17
        adapter = new PagerAdapter(manager);
        pager = (ViewPager) view.findViewById(R.id.pager);
        pager.setAdapter(adapter);
        return(view);
    }
}
