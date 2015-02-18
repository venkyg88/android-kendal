package com.staples.mobile.cfa.login;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.staples.mobile.cfa.R;
import com.staples.mobile.cfa.widget.ActionBar;

/**
 * Created by Avinash Dodda.
 */
public class ResetPasswordFragment extends Fragment {
    private static final String TAG = "ResetPasswordFragment";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        ViewGroup view = (ViewGroup) inflater.inflate(R.layout.reset_password_fragment, container, false);
        return(view);
    }

    @Override
    public void onResume() {
        super.onResume();
        ActionBar.getInstance().setConfig(ActionBar.Config.PASSWORD);
    }
}
