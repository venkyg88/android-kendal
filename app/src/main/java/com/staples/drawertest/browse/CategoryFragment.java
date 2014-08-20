package com.staples.drawertest.browse;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.staples.drawertest.R;

/**
 * Created by PyhRe001 on 8/11/14.
 */
public class CategoryFragment extends Fragment {
    private static final String TAG = "TopCategoryFragment";

    private CategoryAdapter adapter;
    private String path;

    public CategoryAdapter getAdapter() {
        return(adapter);
    }

    public String getPath() {
        return(path);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        Log.d(TAG, "onCreateView()");
        View view = inflater.inflate(R.layout.category, container, false);

        Bundle args = getArguments();
        Log.d(TAG, "getArguments() "+args);
        String title = args.getString("title");
        ((TextView) view.findViewById(R.id.title)).setText(title);
        path = args.getString("path");

        adapter = new CategoryAdapter(getActivity());
        ListView categories = (ListView) view.findViewById(R.id.categories);
        categories.setAdapter(adapter);

        new MidCategoryFiller().execute(this);

        return(view);
    }
}
