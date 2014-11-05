package com.staples.mobile.cfa.profile;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.staples.mobile.R;

/**
 * Created by Avinash Raja Dodda.
 */
public class ShippingFragment extends Fragment{

    private static final String TAG = "Add Shipping Fragment";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {

        Log.d(TAG, "onCreateView()");
        View view = inflater.inflate(R.layout.addshipping_fragment, container, false);
        return (view);
    }

}
