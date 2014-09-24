package com.staples.mobile.sku;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.staples.mobile.R;

public class SkuFragment extends Fragment {
    private static final String TAG = "SkuFragment";

    private ViewPager pager;
    private SkuAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        Log.d(TAG, "onCreateView()");
        View view = inflater.inflate(R.layout.lms_frame, container, false);

        adapter = new SkuAdapter(getActivity());
        pager = (ViewPager) view.findViewById(R.id.pager);
        pager.setAdapter(adapter);

        adapter.fill();

        return (view);
    }
}
