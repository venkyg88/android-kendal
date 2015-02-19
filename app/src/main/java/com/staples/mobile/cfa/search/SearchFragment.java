package com.staples.mobile.cfa.search;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.staples.mobile.cfa.MainActivity;
import com.staples.mobile.cfa.R;
import com.staples.mobile.cfa.analytics.Tracker;
import com.staples.mobile.cfa.bundle.BundleAdapter;
import com.staples.mobile.cfa.bundle.BundleItem;
import com.staples.mobile.cfa.widget.ActionBar;
import com.staples.mobile.cfa.widget.DataWrapper;
import com.staples.mobile.cfa.widget.HorizontalDivider;
import com.staples.mobile.common.access.Access;
import com.staples.mobile.common.access.easyopen.api.EasyOpenApi;
import com.staples.mobile.common.access.easyopen.model.ApiError;
import com.staples.mobile.common.access.easyopen.model.browse.Search;
import com.staples.mobile.common.access.easyopen.model.browse.SearchResult;

import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class SearchFragment extends Fragment implements Callback<SearchResult>, View.OnClickListener {
    private static final String TAG = "SearchFragment";

    private static final String KEYWORD = "keyword";

    private static final int MAXFETCH = 50;
    private static final int SORT_BY_BEST_MATCH = 0;

    private DataWrapper wrapper;
    private BundleAdapter adapter;

    public void setArguments(String keyword) {
        Bundle args = new Bundle();
        args.putString(KEYWORD, keyword);
        setArguments(args);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        String keyword = null;

        View view = inflater.inflate(R.layout.bundle_frame, container, false);

        Bundle args = getArguments();
        if (args!=null) {
            keyword = args.getString(KEYWORD);
        }

        wrapper = (DataWrapper) view.findViewById(R.id.wrapper);
        RecyclerView products = (RecyclerView) view.findViewById(R.id.products);
        adapter = new BundleAdapter(getActivity());
        products.setAdapter(adapter);
        products.setLayoutManager(new GridLayoutManager(getActivity(), 1));
        products.addItemDecoration(new HorizontalDivider(getActivity()));
        adapter.setOnClickListener(this);

        wrapper.setState(DataWrapper.State.LOADING);
        EasyOpenApi easyOpenApi = Access.getInstance().getEasyOpenApi(false);
        easyOpenApi.searchResult(keyword, 1, MAXFETCH, SORT_BY_BEST_MATCH, null, this);

        return (view);
    }

    @Override
    public void onResume() {
        super.onResume();
        ActionBar.getInstance().setConfig(ActionBar.Config.SEARCH);
    }

    @Override
    public void success(SearchResult searchResult, Response response) {
        Activity activity = getActivity();
        if (activity==null) return;

        int count = processSearch(searchResult);
        if (count==0) wrapper.setState(DataWrapper.State.EMPTY);
        else wrapper.setState(DataWrapper.State.DONE);
        adapter.notifyDataSetChanged();

        //Analytics
        //@TODO get(0) again
        //get the actual count of search results
        int countR = searchResult.getSearch().get(0).getItemCount();
        //@TODO quesry string is the term
        Tracker.getInstance().trackStateForSearchResults("query string-tbd", countR); //Analytics

    }

    @Override
    public void failure(RetrofitError retrofitError) {
        Activity activity = getActivity();
        if (activity==null) return;

        String msg = ApiError.getErrorMessage(retrofitError);
        ((MainActivity) activity).showErrorDialog(msg);
        wrapper.setState(DataWrapper.State.EMPTY);
        Log.d(TAG, msg);
    }

    private int processSearch(SearchResult searchResult) {
        if (searchResult==null) return(0);
        List<Search> searches = searchResult.getSearch();
        if (searches==null || searches.size()<1) return(0);
        Search search = searches.get(0);
        if (search==null) return(0);
        int count = adapter.fill(search.getProduct());
        return(count);
    }

    @Override
    public void onClick(View view) {
        Object tag;
        switch(view.getId()) {
            case R.id.bundle_item:
                tag = view.getTag();
                if (tag instanceof BundleItem) {
                    BundleItem item = (BundleItem) tag;
                    ((MainActivity) getActivity()).selectSkuItem(item.title, item.identifier, false);
                }
                break;
            case R.id.bundle_action:
                tag = view.getTag();
                if (tag instanceof BundleItem) {
                    BundleItem item = (BundleItem) tag;
                    Toast.makeText(getActivity(), "Clicked on " + item.title, Toast.LENGTH_LONG).show();
                }
                break;
        }
    }
}
