package com.staples.mobile.feed;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.staples.mobile.R;

public class PersonalFeedFragment extends Fragment {
    private static final String TAG = "PersonalFeedFragment";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        View item;

        Log.d(TAG, "onCreateView()");
        ViewGroup group = (ViewGroup) inflater.inflate(R.layout.personal_feed, container, false);

        inflater.inflate(R.layout.your_store, group);

        item = inflater.inflate(R.layout.order_track, group, false);
        item.setBackgroundColor(0xffccffcc);
        ((TextView) item.findViewById(R.id.icon)).setText("✓");
        ((TextView) item.findViewById(R.id.status)).setText("Your order has been shipped\n#123456");
        group.addView(item);

        item = inflater.inflate(R.layout.order_track, group, false);
        item.setBackgroundColor(0xffffcccc);
        ((TextView) item.findViewById(R.id.icon)).setText("X");
        ((TextView) item.findViewById(R.id.status)).setText("Your order has been back-ordered\n#234567");
        group.addView(item);

        inflater.inflate(R.layout.rewards, group);

        return(group);
    }
}
