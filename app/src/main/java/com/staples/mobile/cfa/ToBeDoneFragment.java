package com.staples.mobile.cfa;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.staples.mobile.R;

public class ToBeDoneFragment extends Fragment {
    private static final String TAG = "ToBeDoneFragment";
    private static String token1;
    private static String token2;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        Log.d(TAG, "onCreateView()");
        View view = inflater.inflate(R.layout.tobedone, container, false);

        Bundle args = getArguments();
        if (args!=null) {
            String title = args.getString("title");
            ((TextView) view.findViewById(R.id.title)).setText(title);
        }
        return(view);
    }
}
