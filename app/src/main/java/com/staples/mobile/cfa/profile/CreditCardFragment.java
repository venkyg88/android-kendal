package com.staples.mobile.cfa.profile;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.staples.mobile.R;

/**
 * Created by Avinash Dodda.
 */
public class CreditCardFragment extends Fragment {

    private static final String TAG = "Add Credit Card Fragment";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {

        Log.d(TAG, "onCreateView()");
        View view = inflater.inflate(R.layout.addcc_fragment, container, false);
        return (view);
    }
}
