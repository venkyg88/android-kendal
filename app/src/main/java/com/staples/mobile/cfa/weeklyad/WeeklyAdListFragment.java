package com.staples.mobile.cfa.weeklyad;


import android.content.res.Resources;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.TabHost;

import com.staples.mobile.cfa.IdentifierType;
import com.staples.mobile.cfa.MainActivity;
import com.staples.mobile.cfa.R;
import com.staples.mobile.cfa.cart.CartApiManager;
import com.staples.mobile.common.analytics.Tracker;
import com.staples.mobile.cfa.widget.ActionBar;
import com.staples.mobile.cfa.widget.HorizontalDivider;
import com.staples.mobile.common.access.Access;
import com.staples.mobile.common.access.easyopen.api.EasyOpenApi;
import com.staples.mobile.common.access.easyopen.model.ApiError;
import com.staples.mobile.common.access.easyopen.model.weeklyad.Data;
import com.staples.mobile.common.access.easyopen.model.weeklyad.WeeklyAd;

import java.util.ArrayList;
import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by Avinash Dodda.
 */

public class WeeklyAdListFragment extends Fragment implements View.OnClickListener {

    private static final String STOREID = "storeid";
    private static final String CATEGORYTREEIDS = "categoryTreeIds";
    private static final String TITLES = "titles";
    private static final String TABINDEX = "tabIndex";

    MainActivity activity;
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    WeeklyAdListAdapter adapter;
    String storeNumber;
    EasyOpenApi easyOpenApi;
    List<Data> weeklyAdItems;
    List<String> categoryTreeIds;
    List<String> titles;
    int currentTabIndex;
    TabHost tabHost;
    HorizontalScrollView tabScrollView;


    public void setArguments(String storeNumber, int currentTabIndex,
                             ArrayList<String> categoryTreeIds, ArrayList<String> titles) {
        Bundle args = new Bundle();
        args.putString(STOREID, storeNumber);
        args.putStringArrayList(CATEGORYTREEIDS, categoryTreeIds);
        args.putStringArrayList(TITLES, titles);
        args.putInt(TABINDEX, currentTabIndex);
        setArguments(args);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        activity = (MainActivity)getActivity();
        View view = inflater.inflate(R.layout.weekly_ad_list, container, false);

        Bundle args = getArguments();
        if (args!=null) {
            storeNumber = args.getString(STOREID);
            categoryTreeIds = args.getStringArrayList(CATEGORYTREEIDS);
            titles = args.getStringArrayList(TITLES);
            currentTabIndex = args.getInt(TABINDEX);
        }

        // set up recycler view
        mRecyclerView = (RecyclerView) view.findViewById(R.id.weekly_ad_list_items);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new GridLayoutManager(activity, 2));
        mRecyclerView.addItemDecoration(new HorizontalDivider(activity));
        adapter = new WeeklyAdListAdapter(activity, this);
        mRecyclerView.setAdapter(adapter);

        // set up tabs
        tabHost = (TabHost) view.findViewById(android.R.id.tabhost);
        tabScrollView = (HorizontalScrollView)view.findViewById(R.id.tabs_scrollview);
        tabHost.setup();
        TabHost.TabContentFactory tabContentFactory = new TabHost.TabContentFactory() {
            @Override public View createTabContent(String tag) { return mRecyclerView; }
        };
        for (int i = 0; i < titles.size(); i++) {
            TabHost.TabSpec tab = tabHost.newTabSpec(String.valueOf(i)); // this is the tabId supplied in onTabChanged
            tab.setIndicator(titles.get(i));
            tab.setContent(tabContentFactory);
            tabHost.addTab(tab);
        }
        tabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {
                currentTabIndex = Integer.parseInt(tabId);
                getArguments().putInt(TABINDEX, currentTabIndex);
                ActionBar.getInstance().setConfig(ActionBar.Config.WEEKLYAD, titles.get(currentTabIndex));
                getWeeklyAdListing();
            }
        });
        tabHost.setCurrentTab(currentTabIndex);

        // initialize scroll position to currently selected tab (delay until position info is available)
        tabHost.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (currentTabIndex > 0) {
                    // scroll such that currently selected tab is centered
                    View currentTabView = tabHost.getCurrentTabView();
                    int targetScrollX = currentTabView.getLeft() - (tabScrollView.getWidth() - currentTabView.getWidth()) / 2;
                    if (currentTabView.getLeft() > targetScrollX) {
                        tabScrollView.scrollTo(targetScrollX, 0);
                    }
                }
            }
        }, 100);


        // initiate call to get weekly ads
        getWeeklyAdListing();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        ActionBar.getInstance().setConfig(ActionBar.Config.WEEKLYAD, titles.get(currentTabIndex));
        Tracker.getInstance().trackStateForWeeklyAd(); // Analytics
    }


    public void getWeeklyAdListing() {
        activity.showProgressIndicator();
        easyOpenApi = Access.getInstance().getEasyOpenApi(false);
        easyOpenApi.getWeeklyAdCategoryListing(storeNumber, categoryTreeIds.get(currentTabIndex),
                new Callback<WeeklyAd>() {
            @Override
            public void success(WeeklyAd weeklyAd, Response response) {
                activity.hideProgressIndicator();
                weeklyAdItems = weeklyAd.getContent().getCollection().getData();
                adapter.fill(weeklyAdItems);
            }

            @Override
            public void failure(RetrofitError error) {
                activity.hideProgressIndicator();
                activity.showErrorDialog(ApiError.getErrorMessage(error));
            }
        });
    }


    @Override
    public void onClick(View view) {
        final Resources r = activity.getResources();
        Object tag;
        switch(view.getId()) {
            case R.id.weekly_ad_list_image: // go to sku page
                tag = view.getTag();
                if (tag instanceof Data) {
                    Data data = (Data) tag;
                    ((MainActivity)getActivity()).selectSkuItem(data.getProductdescription(), data.getRetailerproductcode(), false);
                    // TODO: the following hasn't been specified, but I expect it's coming
                    // Tracker.getInstance().trackActionForWeeklyAdSelection(adapter.getItemPosition(item), 1); // analytics
                }
                break;
            case R.id.weeklyad_sku_action: // add to cart
                tag = view.getTag();
                if (tag instanceof Data) {
                    final Data data = (Data) tag;
                    final ImageView buttonVw = (ImageView)view;
                    activity.showProgressIndicator();
                    buttonVw.setImageDrawable(r.getDrawable(R.drawable.ic_android));
                    CartApiManager.addItemToCart(data.getRetailerproductcode(), 1, new CartApiManager.CartRefreshCallback() {
                        @Override
                        public void onCartRefreshComplete(String errMsg) {
                            activity.hideProgressIndicator();
                            ActionBar.getInstance().setCartCount(CartApiManager.getCartTotalItems());
                            // if success
                            if (errMsg == null) {
                                buttonVw.setImageDrawable(r.getDrawable(R.drawable.added_to_cart));
                                activity.showNotificationBanner(R.string.cart_updated_msg);

                                // if price is parseable, send analytics
                                if (data.getPrice().startsWith("$")) {
                                    String price = data.getPrice().substring(1);
                                    try {
                                        float priceValue = Float.parseFloat(price);
                                        Tracker.getInstance().trackActionForAddToCartFromClass(data.getRetailerproductcode(), priceValue, 1);
                                    } catch (NumberFormatException e) {}
                                }
                            } else {
                                buttonVw.setImageDrawable(r.getDrawable(R.drawable.add_to_cart));
                                // if non-grammatical out-of-stock message from api, provide a nicer message
                                if (errMsg.contains("items is out of stock")) {
                                    errMsg = r.getString(R.string.avail_outofstock);
                                }
                                activity.showErrorDialog(errMsg);
                            }
                        }
                    });
                }
                break;
        }

    }
}