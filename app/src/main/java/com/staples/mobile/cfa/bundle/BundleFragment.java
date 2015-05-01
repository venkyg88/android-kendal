package com.staples.mobile.cfa.bundle;

import android.app.Activity;
import android.app.Dialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.crittercism.app.Crittercism;
import com.staples.mobile.cfa.IdentifierType;
import com.staples.mobile.cfa.MainActivity;
import com.staples.mobile.cfa.R;
import com.staples.mobile.cfa.cart.CartApiManager;
import com.staples.mobile.cfa.widget.ActionBar;
import com.staples.mobile.cfa.widget.DataWrapper;
import com.staples.mobile.cfa.widget.HorizontalDivider;
import com.staples.mobile.cfa.widget.SortPanel;
import com.staples.mobile.common.access.Access;
import com.staples.mobile.common.access.easyopen.api.EasyOpenApi;
import com.staples.mobile.common.access.easyopen.model.ApiError;
import com.staples.mobile.common.access.easyopen.model.browse.Browse;
import com.staples.mobile.common.access.easyopen.model.browse.Category;
import com.staples.mobile.common.analytics.Tracker;

import java.util.Comparator;
import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class BundleFragment extends Fragment implements Callback<Browse>, BundleAdapter.OnFetchMoreData, View.OnClickListener, DialogInterface.OnDismissListener {
    private static final String TAG = BundleFragment.class.getSimpleName();

    private enum SortOption {DENY, ALLOW, VISIBLE};

    private static final String TITLE = "title";
    private static final String IDENTIFIER = "identifier";

    private static final int MAXFETCH = 50;
    private static final int LOOKAHEAD = 12;

    private String title;
    private String identifier;
    private RecyclerView list;
    private BundleAdapter adapter;
    private DataWrapper.State state;
    private boolean complete;
    private int page;
    private SortOption sortOption;
    private BundleItem.SortType fetchSort;
    private BundleItem.SortType displaySort;
    private Dialog popup;

    public void setArguments(String title, String identifier) {
        Bundle args = new Bundle();
        args.putString(TITLE, title);
        args.putString(IDENTIFIER, identifier);
        setArguments(args);
    }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        Bundle args = getArguments();
        if (args!=null) {
            title = args.getString(TITLE);
            identifier = args.getString(IDENTIFIER);
        }

        IdentifierType type = IdentifierType.detect(identifier);
        if (type==IdentifierType.BUNDLE) sortOption = SortOption.DENY;
        else sortOption = SortOption.ALLOW;
        page = 1;
        displaySort = BundleItem.SortType.BESTMATCH;
        query();
        state = DataWrapper.State.LOADING;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        Crittercism.leaveBreadcrumb("BundleFragment:onCreateView(): Displaying the Bundle screen.");
        Activity activity = getActivity();
        View view = inflater.inflate(R.layout.bundle_frame, container, false);

        list = (RecyclerView) view.findViewById(R.id.products);
        list.setLayoutManager(new GridLayoutManager(activity, 1));
        list.addItemDecoration(new HorizontalDivider(activity));

        // Disable sort on bundles
        IdentifierType type = IdentifierType.detect(identifier);

        applyState(view);
        return(view);
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
        wrapper.setState(state);
    }

    @Override
    public void onResume() {
        super.onResume();
        ActionBar.getInstance().setConfig(ActionBar.Config.BUNDLE, title);
    }

    @Override
    public void onFetchMoreData() {
        page++;
        query();
    }

    private void query() {
        fetchSort = displaySort;

        EasyOpenApi easyOpenApi = Access.getInstance().getEasyOpenApi(false);
        easyOpenApi.getCategory(identifier, page, MAXFETCH, null, fetchSort.stringParam, this);
    }

    @Override
    public void success(Browse browse, Response response) {
        Activity activity = getActivity();
        if (!(activity instanceof MainActivity)) return;

        int count = processBrowse(browse);
        if (count==0) {
            state = DataWrapper.State.EMPTY;
        } else {
            state = DataWrapper.State.DONE;
            if (sortOption==SortOption.ALLOW) sortOption = SortOption.VISIBLE;
        }
        applyState(null);

        Tracker.getInstance().trackStateForClass(count, browse, Tracker.ViewType.GRID, fetchSort.stringParam); // Analytics
    }

    @Override
    public void failure(RetrofitError retrofitError) {
        Activity activity = getActivity();
        if (!(activity instanceof MainActivity)) return;

        String msg = ApiError.getErrorMessage(retrofitError);
        Log.d(TAG, msg);
        state = DataWrapper.State.EMPTY;
        applyState(null);
        showPopup();
    }

    private int processBrowse(Browse browse) {
        if (browse==null) return(0);
        complete = (browse.getRecordSetTotal()<=page*MAXFETCH);

        List<Category> categories = browse.getCategory();
        if (categories==null || categories.size()<1) return(0);
        Category category = categories.get(0);
        if (category==null) return(0);

        // Create adaptor
        if (adapter==null) {
            adapter = new BundleAdapter(getActivity());
            adapter.setOnClickListener(this);
        }

        // Add straight products
        adapter.fill(category.getProduct());

        // Add promos (in bundle)
        List<Category> promos = category.getPromoCategory();
        if (promos!=null) {
            for(Category promo : promos)
                adapter.fill(promo.getProduct());
        }

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

            ((MainActivity) activity).swallowTouchEvents(false);
            item.busy = false;
            adapter.notifyDataSetChanged();

            // if success
            if (errMsg == null) {
                ActionBar.getInstance().setCartCount(CartApiManager.getCartTotalItems());
                Tracker.getInstance().trackActionForAddToCartFromClass(CartApiManager.getCartProduct(item.identifier), 1);
            } else {
                // if non-grammatical out-of-stock message from api, provide a nicer message
                if (errMsg.contains("items is out of stock")) {
                    errMsg = activity.getResources().getString(R.string.avail_outofstock);
                }
                ((MainActivity) activity).showErrorDialog(errMsg);
            }
        }
    }

    private void showPopup() {
        popup = new Dialog(getActivity());
        Window window = popup.getWindow();
        window.requestFeature(Window.FEATURE_NO_TITLE);
        window.setBackgroundDrawableResource(R.drawable.dialog_frame);
        popup.setContentView(R.layout.expired_dialog);
        popup.findViewById(R.id.ok).setOnClickListener(this);
        popup.setCanceledOnTouchOutside(false);
        popup.setOnDismissListener(this);
        popup.show();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        getFragmentManager().popBackStack();
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
                    Tracker.getInstance().trackActionForClassItemSelection(adapter.getItemPosition(item), 1); // analytics
                }
                break;
            case R.id.bundle_action:
                tag = view.getTag();
                if (tag instanceof BundleItem) {
                    BundleItem item = (BundleItem) tag;
                    Tracker.getInstance().trackActionForClassItemSelection(adapter.getItemPosition(item), 1); // analytics
                    if (item.type==IdentifierType.SKUSET) {
                        MainActivity activity = (MainActivity) getActivity();
                        activity.selectSkuItem(item.title, item.identifier, false);
                    } else {
                        new AddToCart(item);
                    }
                }
                break;
            case R.id.open_sort:
                SortPanel panel = new SortPanel(getActivity());
                panel.setSelectedRadioButton(displaySort.button);
                panel.setOnClickListener(this);
                panel.show();
                break;
            case R.id.ok:
                if (popup!=null) popup.dismiss();
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
