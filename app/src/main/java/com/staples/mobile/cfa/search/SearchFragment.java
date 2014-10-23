package com.staples.mobile.cfa.search;

/**
 * Author: Yongnan Zhou
 */

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;

import com.staples.mobile.R;
import com.staples.mobile.cfa.MainActivity;

public class SearchFragment extends Fragment implements AdapterView.OnItemClickListener {
    private static final String TAG = "SearchFragment";

    // Initialize UI elements
    private SearchAdapter searchAdapter;
    private SearchWrapperLayout wrapper;
    private View view;
    private GridView searchResultListView;

    // Initialize Api variables
    public static String keyword = "";

    // Initialize UI elements
    private TextView sortMethodView1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        Log.d(TAG, "SearchFragment:onCreateView()" + " this[" + this + "]");
        view = inflater.inflate(R.layout.search_result, container, false);
        wrapper = (SearchWrapperLayout) view.findViewById(R.id.wrapper);

        findViews();
        setAdapter();
        setListeners();

        // execute search call
        searchAdapter.getSearchResults(keyword);
        return (view);
    }

    @Override
    public void onItemClick(AdapterView parent, View view, int position, long id) {
        SearchResultRowItem item = (SearchResultRowItem) parent.getItemAtPosition(position);
        if (item == null || item.getProduceName() == null) {
            return;
        }
        ((MainActivity) getActivity()).selectSkuItem(item.getSku());
    }

    private void findViews(){
        searchResultListView = (GridView) view.findViewById(R.id.result_list);
        searchResultListView.setSmoothScrollbarEnabled(true);
        sortMethodView1 = (TextView) view.findViewById(R.id.sort_1);
    }

    private void setAdapter(){
        searchAdapter = new SearchAdapter(getActivity(), wrapper);
        searchResultListView.setAdapter(searchAdapter);
        searchResultListView.setOnItemClickListener(this);
    }

    private void setListeners(){
        sortMethodView1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View viewIn) {
                try {
                    // Swap Fragments
//                    SearchFragment sf = new SearchFragment();
//                    FragmentManager manager = getFragmentManager();
//                    FragmentTransaction transaction = manager.beginTransaction();
//                    transaction.replace(R.id.content, sf);
//                    transaction.commit();

                    searchAdapter.getSearchResults(keyword);
                } catch (Exception e) {
                    Log.e(TAG+"Sort by best match", e.toString());
                }
            }
        });
    }
}
