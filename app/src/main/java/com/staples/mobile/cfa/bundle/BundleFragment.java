package com.staples.mobile.cfa.bundle;

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
import android.widget.ImageView;

import com.staples.mobile.cfa.IdentifierType;
import com.staples.mobile.cfa.MainActivity;
import com.staples.mobile.cfa.R;
import com.staples.mobile.cfa.cart.CartApiManager;
import com.staples.mobile.cfa.widget.ActionBar;
import com.staples.mobile.cfa.widget.DataWrapper;
import com.staples.mobile.cfa.widget.HorizontalDivider;
import com.staples.mobile.common.access.Access;
import com.staples.mobile.common.access.easyopen.api.EasyOpenApi;
import com.staples.mobile.common.access.easyopen.model.ApiError;
import com.staples.mobile.common.access.easyopen.model.browse.Browse;
import com.staples.mobile.common.access.easyopen.model.browse.Category;
import com.staples.mobile.common.analytics.Tracker;

import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class BundleFragment extends Fragment implements Callback<Browse>, View.OnClickListener {
    private static final String TAG = "BundleFragment";

    private static final String TITLE = "title";
    private static final String IDENTIFIER = "identifier";

    private static final int MAXFETCH = 50;

    private BundleAdapter adapter;
    private DataWrapper.State state;
    private String title;

    private Dialog panel;

    public void setArguments(String title, String identifier) {
        Bundle args = new Bundle();
        args.putString(TITLE, title);
        args.putString(IDENTIFIER, identifier);
        setArguments(args);
    }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        String identifier = null;
        Bundle args = getArguments();
        if (args!=null) {
            title = args.getString(TITLE);
            identifier = args.getString(IDENTIFIER);
        }

        EasyOpenApi easyOpenApi = Access.getInstance().getEasyOpenApi(false);
        easyOpenApi.browseCategories(identifier, null, MAXFETCH, this);
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
        return(view);
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
        ActionBar.getInstance().setConfig(ActionBar.Config.BUNDLE, title);
        Tracker.getInstance().trackStateForShopByCategory(); // Analytics
    }

    @Override
    public void success(Browse browse, Response response) {
        Activity activity = getActivity();
        if (activity==null) return;

        int count = processBrowse(browse);
        if (count==0) state = DataWrapper.State.EMPTY;
        else state = DataWrapper.State.DONE;
        applyState(null);

        Tracker.getInstance().trackStateForClass(count, browse, Tracker.ViewType.GRID); // Analytics
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

    private int processBrowse(Browse browse) {
        if (browse==null) return(0);
        List<Category> categories = browse.getCategory();
        if (categories==null || categories.size()<1) return(0);
        Category category = categories.get(0);
        if (category==null) return(0);

        // Create adapter
        adapter = new BundleAdapter(getActivity());
        adapter.setOnClickListener(this);

        // Add straight products
        adapter.fill(category.getProduct());

        // Add promos (in bundle)
        List<Category> promos = category.getPromoCategory();
        if (promos!=null) {
            for(Category promo : promos)
                adapter.fill(promo.getProduct());
        }

        adapter.sort(BundleItem.SortType.PRICEASCENDING.getComparator());
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
                    ((MainActivity)getActivity()).selectSkuItem(item.title, item.identifier, false);
                    Tracker.getInstance().trackActionForClassItemSelection(adapter.getItemPosition(item), 1); // analytics
                }
                break;
            case R.id.bundle_action:
                tag = view.getTag();
                if (tag instanceof BundleItem) {
                    final BundleItem item = (BundleItem) tag;
                    Tracker.getInstance().trackActionForClassItemSelection(adapter.getItemPosition(item), 1); // analytics
                    if (item.type==IdentifierType.SKUSET) {
                        final MainActivity activity = (MainActivity) getActivity();
                        activity.selectSkuItem(item.title, item.identifier, false);
                    } else {
                        final MainActivity activity = (MainActivity) getActivity();
                        final ImageView buttonVw = (ImageView)view;
                        activity.showProgressIndicator();
                        buttonVw.setImageDrawable(buttonVw.getResources().getDrawable(R.drawable.ic_android));
                        CartApiManager.addItemToCart(item.identifier, 1, new CartApiManager.CartRefreshCallback() {
                            @Override
                            public void onCartRefreshComplete(String errMsg) {
                                activity.hideProgressIndicator();
                                ActionBar.getInstance().setCartCount(CartApiManager.getCartTotalItems());
                                // if success
                                if (errMsg == null) {
                                    buttonVw.setImageDrawable(buttonVw.getResources().getDrawable(R.drawable.added_to_cart));
                                    activity.showNotificationBanner(R.string.cart_updated_msg);
                                    Tracker.getInstance().trackActionForAddToCartFromClass(item.identifier, item.price, 1);
                                } else {
                                    buttonVw.setImageDrawable(buttonVw.getResources().getDrawable(R.drawable.ic_add_shopping_cart_black));
                                    // if non-grammatical out-of-stock message from api, provide a nicer message
                                    if (errMsg.contains("items is out of stock")) {
                                        errMsg = activity.getResources().getString(R.string.avail_outofstock);
                                    }
                                    activity.showErrorDialog(errMsg);
                                }
                            }
                        });
                    }
                }
                break;
            case R.id.open_sort:
                showPanel();
                break;
        }
    }

    private void showPanel() {
        panel = new Dialog(getActivity());
        Window window = panel.getWindow();
        window.requestFeature(Window.FEATURE_NO_TITLE);
        panel.setContentView(R.layout.sort_panel);
        WindowManager.LayoutParams params = window.getAttributes();
        params.type = WindowManager.LayoutParams.TYPE_APPLICATION_PANEL;
        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        params.gravity = Gravity.BOTTOM;
        params.windowAnimations = R.style.PanelStyle;
        panel.show();
    }
}
