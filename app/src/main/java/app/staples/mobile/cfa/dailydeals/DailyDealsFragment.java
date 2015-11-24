package app.staples.mobile.cfa.dailydeals;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.crittercism.app.Crittercism;
import com.staples.mobile.common.access.Access;
import com.staples.mobile.common.access.easyopen.api.EasyOpenApi;
import com.staples.mobile.common.access.easyopen.model.ApiError;
import com.staples.mobile.common.access.easyopen.model.dailydeals.Browse;
import com.staples.mobile.common.access.easyopen.model.dailydeals.Categories;
import com.staples.mobile.common.analytics.Tracker;

import java.util.List;

import app.staples.R;
import app.staples.mobile.cfa.MainActivity;

import app.staples.mobile.cfa.cart.CartApiManager;
import app.staples.mobile.cfa.widget.ActionBar;
import app.staples.mobile.cfa.widget.DataWrapper;
import app.staples.mobile.cfa.widget.HorizontalDivider;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class DailyDealsFragment extends Fragment implements Callback<Browse> ,DailyDealsAdapter.OnFetchMoreData,View.OnClickListener,DialogInterface.OnDismissListener{

    private static final String TAG = DailyDealsFragment.class.getSimpleName();

    private final static int MAXFETCH = 50;
    private final static int zipcode = 01702;
    private Dialog popup;
    private static final String TITLE = "title";
    private static final String IDENTIFIER = "identifier";
    private static final int LOOKAHEAD = 12;

    private RecyclerView list;
    private DailyDealsAdapter adapter;
    private DataWrapper.State state;
    private int offset;
    private String title;
    private String identifier;
    private boolean complete;

    public void setArguments(String title, String identifier)
    {
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
        offset = 0;
        query();
        state = DataWrapper.State.LOADING;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Crittercism.leaveBreadcrumb("DailyDealsFragment:onCreateView(): Displaying the Daily Deals.");
        Activity activity = getActivity();
        View view = inflater.inflate(R.layout.fragment_daily_deals, container, false);
        view.setTag(this);

        list = (RecyclerView) view.findViewById(R.id.products);
        list.setLayoutManager(new GridLayoutManager(activity, 1));
        list.addItemDecoration(new HorizontalDivider(activity));

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
        wrapper.setState(state);
    }

    @Override
    public void onResume() {
        super.onResume();
        ActionBar.getInstance().setConfig(ActionBar.Config.BUNDLE, title);
    }

    @Override
    public void onFetchMoreData() {
        offset++;
        query();
    }

    private void query() {
        Access access = Access.getInstance();
        EasyOpenApi easyOpenApi = access.getEasyOpenApi(false); // not secure
        easyOpenApi.getDailyDeals(identifier, zipcode, offset, MAXFETCH, this);
    }

    @Override
    public void success(Browse browse, Response response) {
        Activity activity = getActivity();
        if (!(activity instanceof MainActivity))
            return;
        int count = processBrowse(browse);
        if (count==0)
            state = DataWrapper.State.EMPTY;
        else
            state = DataWrapper.State.DONE;
        applyState(null);

        // TODO: Confirmation on this.
        // Tracker.getInstance().trackStateForClass(count, browse, Tracker.ViewType.GRID, fetchSort.stringParam);
    }

    @Override
    public void failure(RetrofitError retrofitError) {
        Activity activity = getActivity();
        if (!(activity instanceof MainActivity)) return;

        Crittercism.logHandledException(retrofitError);
        String msg = ApiError.getErrorMessage(retrofitError);
        Log.d(TAG, msg);
        state = DataWrapper.State.EMPTY;
        applyState(null);
        showPopup();
    }

    private int processBrowse(com.staples.mobile.common.access.easyopen.model.dailydeals.Browse browse) {
        if (browse==null)
            return(0);

        List<Categories> categories = browse.getCategories();

        if (categories==null || categories.size()==0)
            return(0);

        Categories category = null;
        for (int i = 0; i < categories.size(); i++) {
            category = categories.get(i);

            int rank = category.getRank();

            if (rank == 1)
                break;
        }

        if (category==null)
            return(0);

        complete = (category.getTotalRecords() <= offset*MAXFETCH);

        // Create adapter
        if (adapter==null) {
            adapter = new DailyDealsAdapter(getActivity());
            adapter.setOnClickListener(this);
        }
        // Add straight products
        adapter.fill(category.getProducts(), identifier);

        adapter.notifyDataSetChanged();

        int count = adapter.getItemCount();

        if (!complete && count>=MAXFETCH)
            adapter.setOnFetchMoreData(this, count-LOOKAHEAD);

        return(count);
    }

    private class AddToCart implements CartApiManager.CartRefreshCallback {
        private DailyDealsItem item;

        private AddToCart(DailyDealsItem item) {
            MainActivity activity = (MainActivity) getActivity();

            this.item = item;
            item.busy = true;
            activity.swallowTouchEvents(true);

            adapter.notifyDataSetChanged();
            CartApiManager.addItemToCart(item.details.getPartNumber(), 1, this);
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
                Tracker.getInstance().trackActionForAddToCartFromClass(CartApiManager.getCartProduct(item.details.getPartNumber()), 1);
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
        MainActivity activity = (MainActivity) getActivity();
        if (activity!=null) {
            activity.popBackStack();
        }
    }

    @Override
    public void onClick(View view) {
        Object tag;
        switch(view.getId()) {
            case R.id.bundle_item:
                tag = view.getTag();
                if (tag instanceof DailyDealsItem) {
                    DailyDealsItem item = (DailyDealsItem) tag;
                    ((MainActivity) getActivity()).selectSkuItem(item.name, item.details.getPartNumber(), false);
                    Tracker.getInstance().trackActionForClassItemSelection(adapter.getItemPosition(item), 1); // analytics
                }
                break;
            case R.id.bundle_action:
                tag = view.getTag();
                if (tag instanceof DailyDealsItem) {
                    DailyDealsItem item = (DailyDealsItem) tag;
                    Tracker.getInstance().trackActionForClassItemSelection(adapter.getItemPosition(item), 1); // analytics
                    if (null != item.details.getSkuSetType()) {
                        MainActivity activity = (MainActivity) getActivity();
                        activity.selectSkuItem(item.name, item.details.getPartNumber(), false);
                    } else {
                        new AddToCart(item);
                    }
                }
                break;
            case R.id.ok:
                if (popup!=null) popup.dismiss();
                break;
        }
    }
}