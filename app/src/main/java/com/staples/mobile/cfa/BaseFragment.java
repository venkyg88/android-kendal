/*
 * Copyright (c) 2014 Staples, Inc. All rights reserved.
 */

package com.staples.mobile.cfa;

import android.app.Fragment;

public abstract class BaseFragment extends Fragment {

    @Override
    public void onResume() {
        super.onResume();

        // set up activity's action bar (override the onResume method to customize the action bar)
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).showStandardActionBar();
        }
    }
}
