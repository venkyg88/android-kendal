package com.staples.mobile.cfa.sku;

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

        View view = inflater.inflate(R.layout.sku_overview, container, false);

        return (view);
    }
}
