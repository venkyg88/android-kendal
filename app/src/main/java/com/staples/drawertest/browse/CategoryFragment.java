package com.staples.drawertest.browse;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.staples.drawertest.R;

/**
 * Created by PyhRe001 on 8/11/14.
 */
public class CategoryFragment extends Fragment
             implements AdapterView.OnItemClickListener {
    private static final String TAG = "CategoryFragment";

    private CategoryAdapter adapter;
    private String path;

    public CategoryAdapter getAdapter() {
        return (adapter);
    }

    public String getPath() {
        return (path);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        Log.d(TAG, "onCreateView()");
        View view = inflater.inflate(R.layout.category, container, false);

        Bundle args = getArguments();
        String title = args.getString("title");
        ((TextView) view.findViewById(R.id.title)).setText(title);
        path = args.getString("path");

        adapter = new CategoryAdapter(getActivity());
        ListView categories = (ListView) view.findViewById(R.id.categories);
        categories.setAdapter(adapter);
        categories.setOnItemClickListener(this);

        new MidCategoryFiller().execute(this);

        return (view);
    }

    @Override
    public void onItemClick(AdapterView parent, View view, int position, long id) {
        CategoryItem item = (CategoryItem) parent.getItemAtPosition(position);
        if (item==null || item.path==null) {
            Log.d(TAG, "Category leaf selected " + item.title);
            return;
        }

        // Make new fragment
        Fragment fragment = Fragment.instantiate(getActivity(), getClass().getName());
        Bundle args = new Bundle();
        args.putString("title", item.title);
        args.putString("path", item.path);
        fragment.setArguments(args);

        // Swap fragments
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.content, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
