package app.staples.mobile.cfa.search;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.apptentive.android.sdk.Apptentive;
import com.crittercism.app.Crittercism;
import com.staples.mobile.common.access.Access;
import com.staples.mobile.common.access.config.StaplesAppContext;
import com.staples.mobile.common.access.config.model.Api;
import com.staples.mobile.common.access.easyopen.model.ApiError;
import com.staples.mobile.common.access.easyopen.model.browse.Search;
import com.staples.mobile.common.access.easyopen.model.browse.SearchResult;
import com.staples.mobile.common.access.easyopen2.api.EasyOpenApi2;
import com.staples.mobile.common.analytics.Tracker;

import java.text.MessageFormat;
import java.util.Comparator;
import java.util.List;

import app.staples.R;
import app.staples.mobile.cfa.MainActivity;
import app.staples.mobile.cfa.apptentive.ApptentiveSdk;
import app.staples.mobile.cfa.bundle.BundleAdapter;
import app.staples.mobile.cfa.bundle.BundleItem;
import app.staples.mobile.cfa.cart.CartApiManager;
import app.staples.mobile.cfa.widget.ActionBar;
import app.staples.mobile.cfa.widget.DataWrapper;
import app.staples.mobile.cfa.widget.HorizontalDivider;
import app.staples.mobile.cfa.widget.SortPanel;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class SearchFragment extends Fragment implements Callback<SearchResult>, BundleAdapter.OnFetchMoreData, View.OnClickListener {
    private static final String TAG = SearchFragment.class.getSimpleName();

    private enum SortOption {DENY, ALLOW, VISIBLE};

    private static final String TITLE = "title";
    private static final String KEYWORD = "keyword";

    private static final int MAXFETCH = 50;
    private static final int LOOKAHEAD = 12;

    private String title;
    private String keyword;
    private RecyclerView list;
    private BundleAdapter adapter;
    private DataWrapper.State state;
    private boolean complete;
    private int page;
    private SortOption sortOption;
    private BundleItem.SortType fetchSort;
    private BundleItem.SortType displaySort;
    private TextView resultsNotFoundHeader;
    private TextView resultsNotFoundSuggestions;

    public void setArguments(String title, String keyword) {
        Bundle args = new Bundle();
        args.putString(TITLE, title);
        args.putString(KEYWORD, keyword);
        setArguments(args);
    }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        Bundle args = getArguments();
        if (args!=null) {
            title = args.getString(TITLE);
            keyword = args.getString(KEYWORD);
        }

        sortOption = SortOption.ALLOW;
        page = 1;
        displaySort = BundleItem.SortType.BESTMATCH;
        query();
        state = DataWrapper.State.LOADING;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        Crittercism.leaveBreadcrumb("SearchFragment:onCreateView(): Displaying the Search screen.");
        Activity activity = getActivity();
        View view = inflater.inflate(R.layout.bundle_frame, container, false);

        list = (RecyclerView) view.findViewById(R.id.products);
        list.setLayoutManager(new GridLayoutManager(activity, 1));
        list.addItemDecoration(new HorizontalDivider(activity));

        resultsNotFoundHeader = (TextView) view.findViewById(R.id.search_results_not_found_header);
        resultsNotFoundSuggestions = (TextView) view.findViewById(R.id.search_results_not_found_suggestions);
        //not set in XML to avoid duplicating bundle_frame.xml
        resultsNotFoundSuggestions.setText(getString(R.string.search_results_not_found_suggestions));

        view.findViewById(R.id.open_sort).setOnClickListener(this);

        applyState(view);
        return (view);
    }

    private void applyState(View view) {
        if (view==null) view = getView();
        if (view==null) return;
        DataWrapper wrapper = (DataWrapper) view.findViewById(R.id.wrapper);
        if (list!=null && list.getAdapter()==null && adapter!=null) {
            list.setAdapter(adapter);
        }
        if (sortOption==SortOption.VISIBLE) {
            view.findViewById(R.id.open_sort).setVisibility(View.VISIBLE);
            view.findViewById(R.id.open_sort).setOnClickListener(this);
        }
        if (state == DataWrapper.State.EMPTY) {
            String header = MessageFormat.format(getString(R.string.search_results_not_found_header), keyword);
            resultsNotFoundHeader.setText(header);
        }
        wrapper.setState(state);
    }

    @Override
    public void onResume() {
        super.onResume();
        ActionBar.getInstance().setConfig(ActionBar.Config.SEARCH, title);
    }

    @Override
    public void onFetchMoreData() {
        page++;
        query();
    }

    private void query() {
        fetchSort = displaySort;

        EasyOpenApi2 easyOpenApi2 = Access.getInstance().getEasyOpenApi2(false);
        Api easy2API = StaplesAppContext.getInstance().getApiByName(StaplesAppContext.EASYOPEN2);
        String version = easy2API.getVersion();
        easyOpenApi2.searchResult(version, keyword, page, MAXFETCH, fetchSort.intParam, null, this);
    }

    @Override
    public void success(SearchResult searchResult, Response response) {
        Activity activity = getActivity();
        if (!(activity instanceof MainActivity)) return;

        int count = processSearch(searchResult);
        if (count==0) {
            state = DataWrapper.State.EMPTY;
        } else {
            state = DataWrapper.State.DONE;
            sortOption = SortOption.VISIBLE;
        }
        applyState(null);

        // Analytics
        if (searchResult.getSearch() != null && searchResult.getSearch().size() > 0) {
            //get the actual count of search results
            Search search = searchResult.getSearch().get(0);
            Tracker.SortType sortType = Tracker.SortType.getBySearchSortNumber(
                    fetchSort.intParam != null? fetchSort.intParam : 0);
            Tracker.getInstance().trackStateForSearchResults(search.getSearchTerm(),
                    search.getItemCount(), Tracker.ViewType.GRID, sortType); //Analytics
            Apptentive.engage(activity, ApptentiveSdk.SEARCH_SHOWN_EVENT);
        }
    }

    @Override
    public void failure(RetrofitError retrofitError) {
        Activity activity = getActivity();
        if (!(activity instanceof MainActivity)) return;

        String msg = ApiError.getErrorMessage(retrofitError);
        ((MainActivity) activity).showErrorDialog(msg);
        Log.d(TAG, msg);
        state = DataWrapper.State.EMPTY;
        applyState(null);
    }

    private int processSearch(SearchResult searchResult) {
        if (searchResult==null) return(0);

        List<Search> searches = searchResult.getSearch();
        if (searches==null || searches.size()<1) return (0);
        Search search = searches.get(0);
        if (search==null) return(0);
        complete = (search.getItemCount()<=page*MAXFETCH);

        // Create adaptor
        if (adapter==null) {
            adapter = new BundleAdapter(getActivity());
            adapter.setOnClickListener(this);
        }

        adapter.fill(search.getProduct());
        adapter.notifyDataSetChanged();

        int count = adapter.getItemCount();
        if (!complete && count>=MAXFETCH)
            adapter.setOnFetchMoreData(this, count-LOOKAHEAD);
        return(count);
    }

    private void performSort() {
        if (adapter==null) return;

        // If complete, a local sort will do
        if (complete) {
            // Can sort locally
            Comparator<BundleItem> comparator;
            if (displaySort==fetchSort) {
                comparator = BundleItem.indexComparator;
            } else {
                comparator = displaySort.comparator;
            }
            if (comparator!=null) {
                adapter.sort(comparator);
                adapter.notifyDataSetChanged();
                list.scrollToPosition(0);
                return;
            }
            else; // Best match desired, but fetch was on other criteria
        }

        // Need to perform a server re-query
        if (adapter!=null) {
            adapter.clear();
            adapter.notifyDataSetChanged();
        }
        page = 1;
        query();
        state = DataWrapper.State.LOADING;
        applyState(null);
    }

    private class AddToCart implements CartApiManager.CartRefreshCallback {
        private BundleItem item;

        private AddToCart(BundleItem item) {
            MainActivity activity = (MainActivity) getActivity();
            this.item = item;
            item.busy = true;
            activity.swallowTouchEvents(true);

            adapter.notifyDataSetChanged();
            CartApiManager.addItemToCart(item.identifier, 1, this);
        }

        @Override
        public void onCartRefreshComplete(String errMsg) {
            Activity activity = getActivity();
            if (!(activity instanceof MainActivity)) return;

            item.busy = false;
            adapter.notifyDataSetChanged();
            ((MainActivity) activity).swallowTouchEvents(false);

            // if success
            if (errMsg == null) {
                ActionBar.getInstance().setCartCount(CartApiManager.getCartTotalItems());
                Tracker.getInstance().trackActionForAddToCartFromSearchResults(CartApiManager.getCartProduct(item.identifier), 1); // analytics
            } else {
                // if non-grammatical out-of-stock message from api, provide a nicer message
                if (errMsg.contains("items is out of stock")) {
                    errMsg = activity.getResources().getString(R.string.avail_outofstock);
                }
                ((MainActivity) activity).showErrorDialog(errMsg);
            }
        }
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
                    BundleItem item = (BundleItem) tag;
                    Tracker.getInstance().trackActionForSearchItemSelection(adapter.getItemPosition(item), 1);
                    new AddToCart(item);
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
                if (sortType!=null) {
                    displaySort = sortType;
                    performSort();
                }
                break;
        }
    }
}
