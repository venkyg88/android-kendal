package com.staples.mobile.cfa.bundle;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.staples.mobile.cfa.IdentifierType;
import com.staples.mobile.cfa.MainActivity;
import com.staples.mobile.cfa.R;
import com.staples.mobile.cfa.analytics.Tracker;
import com.staples.mobile.cfa.cart.CartApiManager;
import com.staples.mobile.cfa.widget.ActionBar;
import com.staples.mobile.cfa.widget.DataWrapper;
import com.staples.mobile.cfa.widget.HorizontalDivider;
import com.staples.mobile.common.access.Access;
import com.staples.mobile.common.access.easyopen.api.EasyOpenApi;
import com.staples.mobile.common.access.easyopen.model.ApiError;
import com.staples.mobile.common.access.easyopen.model.browse.Browse;
import com.staples.mobile.common.access.easyopen.model.browse.Category;

import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class BundleFragment extends Fragment implements Callback<Browse>, View.OnClickListener {
    private static final String TAG = "BundleFragment";

    private static final String TITLE = "title";
    private static final String IDENTIFIER = "identifier";

    private static final int MAXFETCH = 50;

    private DataWrapper wrapper;
    private BundleAdapter adapter;
    private String title;

    public void setArguments(String title, String identifier) {
        Bundle args = new Bundle();
        args.putString(TITLE, title);
        args.putString(IDENTIFIER, identifier);
        setArguments(args);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        String identifier = null;
        View view = inflater.inflate(R.layout.bundle_frame, container, false);

        Bundle args = getArguments();
        if (args!=null) {
            title = args.getString(TITLE);
            identifier = args.getString(IDENTIFIER);
        }

        wrapper = (DataWrapper) view.findViewById(R.id.wrapper);
        RecyclerView list = (RecyclerView) wrapper.findViewById(R.id.products);
        adapter = new BundleAdapter(getActivity());
        list.setAdapter(adapter);
        list.setLayoutManager(new GridLayoutManager(getActivity(), 1));
        list.addItemDecoration(new HorizontalDivider(getActivity()));
        adapter.setOnClickListener(this);

        wrapper.setState(DataWrapper.State.LOADING);
        EasyOpenApi easyOpenApi = Access.getInstance().getEasyOpenApi(false);
        easyOpenApi.browseCategories(identifier, null, MAXFETCH, this);

        return (view);
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
        if (count==0) wrapper.setState(DataWrapper.State.EMPTY);
        else wrapper.setState(DataWrapper.State.DONE);
        adapter.notifyDataSetChanged();
        Tracker.getInstance().trackStateForClass(count, browse, Tracker.ViewType.GRID); // analytics
    }

    @Override
    public void failure(RetrofitError retrofitError) {
        Activity activity = getActivity();
        if (activity==null) return;

        String msg = ApiError.getErrorMessage(retrofitError);
        ((MainActivity)activity).showErrorDialog(msg);
        Log.d(TAG, msg);
        wrapper.setState(DataWrapper.State.EMPTY);
    }

    private int processBrowse(Browse browse) {
        if (browse==null) return(0);
        List<Category> categories = browse.getCategory();
        if (categories==null || categories.size()<1) return(0);
        Category category = categories.get(0);
        if (category==null) return(0);

        // Add straight products
        int count = adapter.fill(category.getProduct());

        // Add promos (in bundle)
        List<Category> promos = category.getPromoCategory();
        if (promos!=null) {
            for(Category promo : promos)
                count += adapter.fill(promo.getProduct());
        }
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
                                    buttonVw.setImageDrawable(buttonVw.getResources().getDrawable(R.drawable.add_to_cart));
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
        }
    }
}
