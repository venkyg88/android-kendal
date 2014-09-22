package com.staples.mobile.lms;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.staples.mobile.R;
import com.staples.mobile.lms.object.Lms;

import retrofit.Callback;

/**
 * Created by PyhRe001 on 8/11/14.
 */
public class LmsFragment extends Fragment {
    private static final String TAG = "LmsFragment";

    private ViewPager pager;
    private LmsAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        Log.d(TAG, "onCreateView()");
        View view = inflater.inflate(R.layout.landing, container, false);

        adapter = new LmsAdapter(getActivity());
        pager = (ViewPager) view.findViewById(R.id.pager);
        pager.setAdapter(adapter);

        adapter.fill();

        return (view);
    }
}
