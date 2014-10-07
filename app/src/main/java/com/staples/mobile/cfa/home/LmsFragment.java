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
    extends Fragment
    implements AdapterView.OnItemClickListener {

    private static final String TAG = "LmsFragment";

    private MainActivity activity = null;
    private LmsAdapter lmsAdapter;
    private ListView bundlesListView;

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

        lmsAdapter = new LmsAdapter(activity);

        bundlesListView = (ListView) view.findViewById(R.id.lsvBundles);
        bundlesListView.setAdapter(lmsAdapter);
        bundlesListView.setOnItemClickListener(this);

        lmsAdapter.fill();

        return (view);
    }

    /* @@@ STUBBED
    public Object instantiateItem(ViewGroup container, int position) {

        LmsItem lmsItem = lmsItems.get(position);
        lmsItem.listView = new ListView(activity);

        // Set adapter
        ProductAdapter productAdapter = new ProductAdapter(activity, lmsItem);
        lmsItem.listView.setOnItemClickListener(this);

        lmsItem.listView.setAdapter(productAdapter);
        productAdapter.fill();

        container.addView(lmsItem.listView);

        return (lmsItem);
    }
    @@@ STUBBED */

    @Override
    public void onItemClick(AdapterView<?> parent,
                            View           clickedView,
                            int            position,
                            long           rowId)
    {
        Log.v(TAG, "LmsFragment:onItemClick():"
                + " position[" + position + "]"
                + " this[" + this + "]"
        );
        /* @@@ STUBBED
        ProductItem productItem = (ProductItem) parent.getItemAtPosition(position);
        activity.selectSkuItem(productItem.identifier);
        @@@ STUBBED */

        return;

    } // onItemClick()
}
