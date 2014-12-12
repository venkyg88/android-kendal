/*
 * Copyright (c) 2014 Staples, Inc. All rights reserved.
 */

package com.staples.mobile.cfa.rewards;

import android.app.Fragment;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TabHost;

import com.staples.mobile.cfa.MainActivity;
import com.staples.mobile.cfa.R;

public class RewardsFragment extends Fragment {
    private static final String TAG = RewardsFragment.class.getSimpleName();
    MainActivity activity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        Resources r = getResources();
        activity = (MainActivity)getActivity();
        ViewGroup view = (ViewGroup) inflater.inflate(R.layout.rewards_fragment, container, false);

        TabHost tabHost = (TabHost) view.findViewById(android.R.id.tabhost);
        tabHost.setup();

        TabHost.TabSpec tab1 = tabHost.newTabSpec("First Tab");
        TabHost.TabSpec tab2 = tabHost.newTabSpec("Second Tab");
        TabHost.TabSpec tab3 = tabHost.newTabSpec("Third Tab");

        tab1.setIndicator(r.getString(R.string.rewards_list_tabtitle));
        tab1.setContent(R.id.tab1_rewards);
        tab2.setIndicator(r.getString(R.string.rewards_ink_tabtitle));
        tab2.setContent(R.id.tab2_ink_recycling);
        tab3.setIndicator(r.getString(R.string.rewards_summary_tabtitle));
        tab3.setContent(R.id.tab3_summary);

        tabHost.addTab(tab1);
        tabHost.addTab(tab2);
        tabHost.addTab(tab3);

        return(view);
    }

    @Override
    public void onResume() {
        super.onResume();
        activity.showActionBar(R.string.rewards_title, 0, null);
    }

}
