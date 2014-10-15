package com.staples.mobile.cfa.bundle;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import com.staples.mobile.R;
import com.staples.mobile.cfa.MainActivity;
import com.staples.mobile.cfa.widget.DataWrapper;

public class BundleFragment extends Fragment implements AdapterView.OnItemClickListener {
    private static final String TAG = "BundleFragment";

    private BundleAdapter adapter;
    private GridView products;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        String path = null;

        Log.d(TAG, "onCreateView()");
        View view = inflater.inflate(R.layout.bundle_frame, container, false);

        Bundle args = getArguments();
        if (args!=null) {
            path = args.getString("path");
        }

        DataWrapper wrapper = (DataWrapper) view.findViewById(R.id.wrapper);
        products = (GridView) view.findViewById(R.id.products);
        adapter = new BundleAdapter(getActivity(), wrapper);
        products.setAdapter(adapter);
        products.setOnItemClickListener(this);

        adapter.fill(path);

        return (view);
    }

    @Override
    public void onItemClick(AdapterView parent, View view, int position, long id) {
        BundleItem item = (BundleItem) parent.getItemAtPosition(position);
        if (item==null || item.identifier==null) {
            return;
        }
        ((MainActivity) getActivity()).selectSkuItem(item.identifier);
    }
}
