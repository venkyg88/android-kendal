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

import com.apptentive.android.sdk.Apptentive;
import com.staples.mobile.cfa.MainActivity;
import com.staples.mobile.cfa.R;
import com.staples.mobile.cfa.apptentive.ApptentiveSdk;
import com.staples.mobile.cfa.bundle.BundleAdapter;
import com.staples.mobile.cfa.bundle.BundleItem;
import com.staples.mobile.cfa.widget.ActionBar;
import com.staples.mobile.cfa.widget.DataWrapper;
import com.staples.mobile.cfa.widget.HorizontalDivider;
import com.staples.mobile.cfa.widget.SortPanel;
import com.staples.mobile.common.access.Access;
import com.staples.mobile.common.access.easyopen.api.EasyOpenApi;
import com.staples.mobile.common.access.easyopen.model.ApiError;
import com.staples.mobile.common.access.easyopen.model.browse.Search;
import com.staples.mobile.common.access.easyopen.model.browse.SearchResult;
import com.staples.mobile.common.analytics.Tracker;

import java.util.Comparator;
import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class SearchFragment extends Fragment implements Callback<SearchResult>, View.OnClickListener {
    private static final String TAG = "SearchFragment";

    private static final String TITLE = "title";
    private static final String KEYWORD = "keyword";

    private static final int MAXFETCH = 50;

    private BundleAdapter adapter;
    private DataWrapper.State state;
    private BundleItem.SortType fetchSort;
    private BundleItem.SortType displaySort;
    private String title;

    public void setArguments(String title, String keyword) {
        Bundle args = new Bundle();
        args.putString(TITLE, title);
        args.putString(KEYWORD, keyword);
        setArguments(args);
    }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        String keyword = null;
        Bundle args = getArguments();
        if (args != null) {
            title = args.getString(TITLE);
            keyword = args.getString(KEYWORD);
        }

        fetchSort = BundleItem.SortType.BESTMATCH;
        displaySort = BundleItem.SortType.BESTMATCH;
        EasyOpenApi easyOpenApi = Access.getInstance().getEasyOpenApi(false);
        easyOpenApi.searchResult(keyword, 1, MAXFETCH, fetchSort.intParam, null, this);
        state = DataWrapper.State.LOADING;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        Activity activity = getActivity();
        View view = inflater.inflate(R.layout.bundle_frame, container, false);
        RecyclerView list = (RecyclerView) view.findViewById(R.id.products);
        list.setLayoutManager(new GridLayoutManager(activity, 1));
        list.addItemDecoration(new HorizontalDivider(activity));

        view.findViewById(R.id.open_sort).setOnClickListener(this);

        applyState(view);
        return (view);
    }

    private void applyState(View view) {
        if (view == null) view = getView();
        if (view == null) return;
        DataWrapper wrapper = (DataWrapper) view.findViewById(R.id.wrapper);
        if (adapter != null) {
            RecyclerView list = (RecyclerView) wrapper.findViewById(R.id.products);
            list.setAdapter(adapter);
        }
        wrapper.setState(state);
    }

    @Override
    public void onResume() {
        super.onResume();
        ActionBar.getInstance().setConfig(ActionBar.Config.SEARCH, title);
    }

    @Override
    public void success(SearchResult searchResult, Response response) {
        Activity activity = getActivity();
        if (activity == null) return;

        int count = processSearch(searchResult);
        if (count == 0) state = DataWrapper.State.EMPTY;
        else state = DataWrapper.State.DONE;
        applyState(null);

        // Analytics
        if (searchResult.getSearch() != null && searchResult.getSearch().size() > 0) {
            //get the actual count of search results
            Search search = searchResult.getSearch().get(0);
            Tracker.getInstance().trackStateForSearchResults(search.getSearchTerm(),
                    search.getItemCount(), Tracker.ViewType.GRID); //Analytics
            Apptentive.engage(activity, ApptentiveSdk.SEARCH_SHOWN_EVENT);
        }
    }

    @Override
    public void failure(RetrofitError retrofitError) {
        Activity activity = getActivity();
        if (activity == null) return;

        String msg = ApiError.getErrorMessage(retrofitError);
        ((MainActivity) activity).showErrorDialog(msg);
        Log.d(TAG, msg);
        state = DataWrapper.State.EMPTY;
        applyState(null);
    }

    private int processSearch(SearchResult searchResult) {
        if (searchResult == null) return (0);
        List<Search> searches = searchResult.getSearch();
        if (searches == null || searches.size() < 1) return (0);
        Search search = searches.get(0);
        if (search == null) return (0);

        adapter = new BundleAdapter(getActivity());
        adapter.setOnClickListener(this);
        adapter.fill(search.getProduct());
        adapter.notifyDataSetChanged();
        return (adapter.getItemCount());
    }

    private void sortLocally() {
        Comparator<BundleItem> comparator;
        if (displaySort == fetchSort) {
            comparator = BundleItem.indexComparator;
        } else {
            comparator = displaySort.comparator;
            if (comparator == null) {
                // TODO Best match wanted, but fetch was not originally by best match
                return;
            }
        }
        adapter.sort(comparator);
        adapter.notifyDataSetChanged();
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
                    Tracker.getInstance().trackActionForSearchItemSelection(adapter.getItemPosition(item), 1);
                }
                break;
            case R.id.bundle_action:
                tag = view.getTag();
                if (tag instanceof BundleItem) {
                    final BundleItem item = (BundleItem) tag;
                    Toast.makeText(getActivity(), "Clicked on " + item.title, Toast.LENGTH_LONG).show();

                    // TODO: !!!!!!!! move this into on add-to-cart success callback similar to BundleFragment !!!!!!!!
                    Tracker.getInstance().trackActionForAddToCartFromSearchResults(item.identifier, item.price, 1);
                }
                break;
            case R.id.open_sort:
                SortPanel panel = new SortPanel(getActivity());
                panel.setSelectedRadioButton(displaySort.button);
                panel.setOnClickListener(this);
                panel.show();
                break;
            default:
                BundleItem.SortType sortType = BundleItem.SortType.findSortTypeById(view.getId());
                if (sortType != null) {
                    displaySort = sortType;
                    sortLocally();
                }
                break;
        }
    }
}