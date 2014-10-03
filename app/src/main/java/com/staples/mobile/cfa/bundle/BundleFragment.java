package com.staples.mobile.cfa.bundle;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.staples.mobile.R;
import com.staples.mobile.cfa.MainActivity;
import com.staples.mobile.cfa.widget.ListViewWrapper;

public class BundleFragment extends Fragment
        implements AdapterView.OnItemClickListener {
    private static final String TAG = "BundleFragment";

    private BundleAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        String path = null;

        Log.d(TAG, "onCreateView()");
        View view = inflater.inflate(R.layout.bundle, container, false);

        Bundle args = getArguments();
        if (args!=null) {
            String title = args.getString("title");
            ((TextView) view.findViewById(R.id.title)).setText(title);
            path = args.getString("path");
        }

        ListViewWrapper wrapper = (ListViewWrapper) view.findViewById(R.id.wrapper);
        adapter = new BundleAdapter(getActivity(), wrapper);
        ListView products = (ListView) view.findViewById(R.id.products);
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
