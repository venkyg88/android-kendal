package com.staples.mobile.cfa.login;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.staples.mobile.R;

public class CreateAccountFragment extends Fragment {

    private static final String TAG = "CreateAccount Fragment";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView()");
        View view =  inflater.inflate(R.layout.create_account, container, false);

        return view;
    }
}
