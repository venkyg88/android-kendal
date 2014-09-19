package com.staples.mobile.lms;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.util.Log;

import com.staples.mobile.ToBeDoneFragment;

/**
 * Created by pyhre001 on 9/16/14.
 */
public class PagerAdapter extends FragmentPagerAdapter {
    private static final String TAG = "PageAdapter";

    public PagerAdapter(FragmentManager manager) {
        super(manager);
    }

    @Override
    public int getCount() {
        return(3);
    }

    @Override
    public Fragment getItem(int position) {
        Log.d(TAG, "getItem of Vertical #"+(position+1));
        Fragment fragment = new ToBeDoneFragment();
        Bundle args = new Bundle();
        args.putString("title", "Vertical #"+(position+1));
        fragment.setArguments(args);

        return(fragment);
    }
}
