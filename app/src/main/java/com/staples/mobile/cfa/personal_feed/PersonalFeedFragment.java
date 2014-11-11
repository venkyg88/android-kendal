package com.staples.mobile.cfa.personal_feed;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.staples.mobile.R;

public class PersonalFeedFragment extends Fragment {
    private static final String TAG = "FeedFragment";

    private PersonalFeedAdapter personalFeedAdapter;
    private View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        Log.d(TAG, "onCreateView()");
        ViewGroup group = (ViewGroup) inflater.inflate(R.layout.personal_feed, container, false);

//        inflater.inflate(R.layout.your_store, group);
//
//        item = inflater.inflate(R.layout.order_track, group, false);
//        item.setBackgroundColor(0xffccffcc);
//        ((TextView) item.findViewById(R.id.icon)).setText("✓");
//        ((TextView) item.findViewById(R.id.status)).setText("Your order has been shipped\n#123456");
//        group.addView(item);

        return (group);
    }
}