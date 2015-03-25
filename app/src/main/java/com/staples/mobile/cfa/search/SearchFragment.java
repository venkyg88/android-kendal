package com.staples.mobile.cfa.search;

import android.app.Activity;
import android.app.Dialog;
import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.apptentive.android.sdk.Apptentive;

import com.staples.mobile.cfa.MainActivity;
import com.staples.mobile.cfa.R;
import com.staples.mobile.common.access.config.StaplesAppContext;
import com.staples.mobile.common.access.configurator.model.Api;
import com.staples.mobile.common.access.easyopen2.api.EasyOpenApi2;
import com.staples.mobile.common.analytics.Tracker;
import com.staples.mobile.cfa.apptentive.ApptentiveSdk;
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

public class SearchFragment extends Fragment implements Callback<SearchResult>, View.OnClickListener, RadioGroup.OnCheckedChangeListener {
    private static final String TAG = "SearchFragment";

    private static final String TITLE = "title";
    private static final String KEYWORD = "keyword";

    private static final int MAXFETCH = 50;
    private static final int SORT_BY_BEST_MATCH = 0;

    private BundleAdapter adapter;
    private DataWrapper.State state;
    private BundleItem.SortType sortType;
    private String title;
    private Dialog panel;

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
        if (args!=null) {
            title = args.getString(TITLE);
            keyword = args.getString(KEYWORD);
        }

        sortType = BundleItem.SortType.ORIGINAL;

        EasyOpenApi2 easyOpenApi2 = Access.getInstance().getEasyOpenApi2(false);
        Api easy2API = StaplesAppContext.getInstance().getApiByName(StaplesAppContext.EASYOPEN2);
        String version = easy2API.getVersion();
        String server = easy2API.getUrl();
        // when tapi is available
        if(server.equals("tapi.staples.com")){
            easyOpenApi2.searchResult(version, keyword, 1, MAXFETCH, SORT_BY_BEST_MATCH, null, this);
        }
        else{
            EasyOpenApi easyOpenApi = Access.getInstance().getEasyOpenApi(false);
            easyOpenApi.searchResult(keyword, 1, MAXFETCH, SORT_BY_BEST_MATCH, null, this);
        }

        state = DataWrapper.State.LOADING;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        View view = inflater.inflate(R.layout.bundle_frame, container, false);
        RecyclerView list = (RecyclerView) view.findViewById(R.id.products);
        list.setLayoutManager(new GridLayoutManager(getActivity(), 1));
        list.addItemDecoration(new HorizontalDivider(getActivity()));

        view.findViewById(R.id.open_sort).setOnClickListener(this);

        applyState(view);
        return (view);
    }

    private void applyState(View view) {
        if (view==null) view = getView();
        if (view==null) return;
        DataWrapper wrapper = (DataWrapper) view.findViewById(R.id.wrapper);
        if (adapter!=null) {
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
        if (activity==null) return;

        int count = processSearch(searchResult);
        if (count==0) state = DataWrapper.State.EMPTY;
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
        if (activity==null) return;

        String msg = ApiError.getErrorMessage(retrofitError);
        ((MainActivity) activity).showErrorDialog(msg);
        Log.d(TAG, msg);
        state = DataWrapper.State.EMPTY;
        applyState(null);
    }

    private int processSearch(SearchResult searchResult) {
        if (searchResult==null) return(0);
        List<Search> searches = searchResult.getSearch();
        if (searches==null || searches.size()<1) return(0);
        Search search = searches.get(0);
        if (search==null) return(0);

        adapter = new BundleAdapter(getActivity());
        adapter.setOnClickListener(this);
        adapter.fill(search.getProduct());
        adapter.notifyDataSetChanged();
        return(adapter.getItemCount());
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
                showPanel();
                break;
        }
    }

    public void onCheckedChanged(RadioGroup group, int id) {
        BundleItem.SortType type = BundleItem.SortType.findSortTypeById(id);
        if (type!=null) {
            panel.dismiss();
            sortType = type;
            adapter.sort(sortType.comparator);
            adapter.notifyDataSetChanged();
        }
    }

    private void showPanel() {
        panel = new Dialog(getActivity());
        Window window = panel.getWindow();
        window.requestFeature(Window.FEATURE_NO_TITLE);
        panel.setContentView(R.layout.sort_panel);

        if (sortType!=null) {
            ((RadioButton) panel.findViewById(sortType.button)).setChecked(true);
        }
        ((RadioGroup) panel.findViewById(R.id.sort_panel)).setOnCheckedChangeListener(this);

        WindowManager.LayoutParams params = window.getAttributes();
        params.type = WindowManager.LayoutParams.TYPE_APPLICATION_PANEL;
        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        params.x = 0;
        params.gravity = Gravity.BOTTOM;
        params.windowAnimations = R.style.PanelAnimation;
        panel.show();
    }
}
