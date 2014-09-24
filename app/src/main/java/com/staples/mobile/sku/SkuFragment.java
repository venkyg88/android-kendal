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
        String identifier = null;

        Log.d(TAG, "onCreateView()");

        Bundle args = getArguments();
        if (args!=null) {
            identifier = args.getString("identifier");
        }

        View view = inflater.inflate(R.layout.pager_frame, container, false);

        adapter = new SkuAdapter(getActivity());
        pager = (ViewPager) view.findViewById(R.id.pager);
        pager.setAdapter(adapter);

        adapter.fill(identifier);

        return (view);
    }
}
