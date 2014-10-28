package com.staples.mobile.cfa.search;

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
import com.staples.mobile.cfa.bundle.BundleAdapter;
import com.staples.mobile.cfa.bundle.BundleItem;
import com.staples.mobile.cfa.widget.DataWrapper;
import com.staples.mobile.common.access.Access;
import com.staples.mobile.common.access.easyopen.api.EasyOpenApi;
import com.staples.mobile.common.access.easyopen.model.browse.Search;
import com.staples.mobile.common.access.easyopen.model.browse.SearchResult;

import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class SearchFragment extends Fragment implements Callback<SearchResult>, AdapterView.OnItemClickListener {
    private static final String TAG = "BundleFragment";

    private static final String RECOMMENDATION = "v1";
    private static final String STORE_ID = "10001";
    private static final String CATALOG_ID = "10051";
    private static final String LOCALE = "en_US";
    private static final String ZIPCODE = "01010";
    private static final String CLIENT_ID = "N6CA89Ti14E6PAbGTr5xsCJ2IGaHzGwS";
    private static final int MAXFETCH = 50;
    private static final int SORT_BY_BEST_MATCH = 0;

    private DataWrapper wrapper;
    private GridView products;
    private BundleAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        String keyword = null;

        Log.d(TAG, "onCreateView()");
        View view = inflater.inflate(R.layout.bundle_frame, container, false);

        Bundle args = getArguments();
        if (args!=null) {
            keyword = args.getString("identifier");
        }

        wrapper = (DataWrapper) view.findViewById(R.id.wrapper);
        products = (GridView) view.findViewById(R.id.products);
        adapter = new BundleAdapter(getActivity());
        products.setAdapter(adapter);
        products.setOnItemClickListener(this);

        fill(keyword);

        return (view);
    }

    private void fill(String keyword) {
        wrapper.setState(DataWrapper.State.LOADING);
        EasyOpenApi easyOpenApi = Access.getInstance().getEasyOpenApi(false);
        easyOpenApi.searchResult(RECOMMENDATION, STORE_ID, CATALOG_ID, LOCALE, ZIPCODE, keyword,
                                 1, MAXFETCH, SORT_BY_BEST_MATCH, CLIENT_ID, null, this);
    }

    @Override
    public void success(SearchResult searchResult, Response response) {
        int count = processSearch(searchResult);
        if (count==0) wrapper.setState(DataWrapper.State.EMPTY);
        else wrapper.setState(DataWrapper.State.DONE);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void failure(RetrofitError retrofitError) {
        Log.d(TAG, "Failure callback " + retrofitError);
        wrapper.setState(DataWrapper.State.EMPTY);
        adapter.notifyDataSetChanged();
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
    public void onItemClick(AdapterView parent, View view, int position, long id) {
        BundleItem item = (BundleItem) parent.getItemAtPosition(position);
        if (item==null || item.identifier==null) {
            return;
        }
        ((MainActivity) getActivity()).selectSkuItem(item.identifier);
    }
}
