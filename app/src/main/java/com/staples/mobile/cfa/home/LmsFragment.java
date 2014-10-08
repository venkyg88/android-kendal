package com.staples.mobile.cfa.home;

import android.app.Activity;
import android.app.Fragment;

import android.os.Bundle;

import android.util.Log;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.AdapterView;
import android.widget.ListView;

import com.staples.mobile.cfa.MainActivity;
import com.staples.mobile.R;

public class LmsFragment
    extends Fragment {

    private static final String TAG = "LmsFragment";

    private MainActivity activity = null;
    private LmsAdapter lmsAdapter;

    @Override
    public void onAttach(Activity activity) {

        Log.v(TAG, "LmsFragment:onAttach():"
                + " activity[" + activity + "]"
                + " this[" + this + "]"
        );
        super.onAttach(activity);

        this.activity = (MainActivity) activity;

        return;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {

        Log.v(TAG, "LmsFragment:onCreateView():"
                + " this[" + this + "]"
        );
        View view = inflater.inflate(R.layout.bundle_frame, container, false);

        lmsAdapter = new LmsAdapter(activity, view);
        lmsAdapter.fill();

        return (view);
    }
}
