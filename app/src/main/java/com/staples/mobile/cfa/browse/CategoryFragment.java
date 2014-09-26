package com.staples.mobile.cfa.browse;

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

public class CategoryFragment extends Fragment
             implements AdapterView.OnItemClickListener {
    private static final String TAG = "CategoryFragment";

    private CategoryAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        String path = null;

        Log.d(TAG, "onCreateView()");
        View view = inflater.inflate(R.layout.category, container, false);

        Bundle args = getArguments();
        if (args!=null) {
            String title = args.getString("title");
            ((TextView) view.findViewById(R.id.title)).setText(title);
            path = args.getString("path");
        }

        adapter = new CategoryAdapter(getActivity());
        ListView categories = (ListView) view.findViewById(R.id.categories);
        categories.setAdapter(adapter);
        categories.setOnItemClickListener(this);
        ListViewWrapper wrapper = (ListViewWrapper) view.findViewById(R.id.status_layout);
        wrapper.setAdapter(adapter);

        adapter.fill(path);

        return (view);
    }

    @Override
    public void onItemClick(AdapterView parent, View view, int position, long id) {
        CategoryItem item = (CategoryItem) parent.getItemAtPosition(position);
        if (item==null || item.path==null) {
            return;
        }

        // Make new fragment
        Fragment fragment = item.instantiate(getActivity());
        Bundle args = new Bundle();
        args.putString("title", item.title);
        args.putString("path", item.path);
        fragment.setArguments(args);

        // Select fragment
        ((MainActivity) getActivity()).selectFragment(fragment, MainActivity.Transition.SLIDE, true);
    }
}
