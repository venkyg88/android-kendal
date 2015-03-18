package com.staples.mobile.cfa.weeklyad;


import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.TabHost;

import com.staples.mobile.cfa.MainActivity;
import com.staples.mobile.cfa.R;
import com.staples.mobile.cfa.cart.CartApiManager;
import com.staples.mobile.common.access.easyopen.model.weeklyadlisting.Collection;
import com.staples.mobile.common.access.easyopen.model.weeklyadlisting.WeeklyAdListing;
import com.staples.mobile.common.access.easyopen.util.WeeklyAdImageUrlHelper;
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
        tabHost.setCurrentTab(currentTabIndex); // call this before setting tab changed listener so we don't trigger it yet
        tabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {
                currentTabIndex = Integer.parseInt(tabId);
                getArguments().putInt(TABINDEX, currentTabIndex);
                scrollToCurrentTab();
                ActionBar.getInstance().setConfig(ActionBar.Config.WEEKLYAD, titles.get(currentTabIndex));
                getWeeklyAdListing();
            }
        });

        // initialize scroll position to currently selected tab (delay until position info is available)
        if (currentTabIndex > 0) {
            tabHost.postDelayed(new Runnable() {
                @Override
                public void run() {
                    scrollToCurrentTab();
                }
            }, 100);
        }

        // set up swipe listener
        View tabbedFrameLayout = view.findViewById(R.id.weekly_ad_list_items);
        final GestureDetectorCompat swipeListener = new GestureDetectorCompat(activity, new LeftRightFlingListener());
        tabbedFrameLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return swipeListener.onTouchEvent(event);
            }
        });



        // initiate call to get weekly ads
        getWeeklyAdListing();

        return view;
    }

    private void scrollToCurrentTab() {
        // scroll such that currently selected tab is centered, unless it's one of the leftmost tabs
        View currentTabView = tabHost.getCurrentTabView();
        int targetScrollX = currentTabView.getLeft() - (tabScrollView.getWidth() - currentTabView.getWidth()) / 2;
        if (currentTabView.getLeft() > targetScrollX) {
            tabScrollView.scrollTo(targetScrollX, 0);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        ActionBar.getInstance().setConfig(ActionBar.Config.WEEKLYAD, titles.get(currentTabIndex));
        Tracker.getInstance().trackStateForWeeklyAd(); // Analytics
    }


    public void getWeeklyAdListing() {
        activity.showProgressIndicator();
        final int imageWidth = (int) activity.getResources().getDimension(R.dimen.weekly_ad_list_item_image_width);
        easyOpenApi = Access.getInstance().getEasyOpenApi(false);
        easyOpenApi.getWeeklyAdCategoryListing(storeNumber, categoryTreeIds.get(currentTabIndex),
                imageWidth, 1, 100,
                new Callback<WeeklyAd>() {
            @Override
            public void success(WeeklyAd weeklyAd, Response response) {
                activity.hideProgressIndicator();
                weeklyAdItems = weeklyAd.getContent().getCollection().getData();
                adapter.fill(weeklyAdItems);
            }

            @Override
            public void failure(RetrofitError error) {

                // DLS: The getWeeklyAdCategoryListing API call sadly returns a different format when count=1
                // and therefore our retrofit code throws a deserialization error. As a workaround,
                // check for the deserialization error, and if present call a special version of
                // getWeeklyAdCategoryListing.
                boolean deserializationError = error.getMessage().contains("not deserialize instance of java.util.ArrayList");
                if (deserializationError) {
                    easyOpenApi.getWeeklyAdCategoryListingSingle(storeNumber, categoryTreeIds.get(currentTabIndex),
                            imageWidth, //1, 1,
                            new Callback<WeeklyAdListing>() {
                                @Override
                                public void success(WeeklyAdListing weeklyAdListing, Response response) {
                                    activity.hideProgressIndicator();
                                    weeklyAdItems = new ArrayList<Data>();
                                    Collection collection = weeklyAdListing.getContent().getCollection();
                                    if (collection != null && collection.getData() != null) {
                                        weeklyAdItems.add(collection.getData());
                                    }
                                    adapter.fill(weeklyAdItems);
                                }

                                @Override
                                public void failure(RetrofitError error) {
                                    activity.hideProgressIndicator();
                                    activity.showErrorDialog(ApiError.getErrorMessage(error));
                                }
                            });
                } else {
                    activity.hideProgressIndicator();
                    activity.showErrorDialog(ApiError.getErrorMessage(error));
                }
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

                    // if in-store item, open expanded image of the ad, otherwise open sku page
                    if (data.getFineprint().contains("In store only") ||
                            TextUtils.isEmpty(data.getRetailerproductcode())) {
                        String imageUrl = WeeklyAdImageUrlHelper.getUrl(
                                (int)r.getDimension(R.dimen.weekly_ad_image_height),
                                (int)r.getDimension(R.dimen.weekly_ad_image_width),
                                data.getImage());
                        ((MainActivity) getActivity()).selectInStoreWeeklyAd(imageUrl,
                                data.getProductdescription(), data.getPrice());
                    } else {
                        // open SKU page
                        ((MainActivity) getActivity()).selectSkuItem(data.getProductdescription(), data.getRetailerproductcode(), false);
                        // TODO: the following hasn't been specified, but I expect it's coming
                        // Tracker.getInstance().trackActionForWeeklyAdSelection(adapter.getItemPosition(item), 1); // analytics
                    }
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


//    GestureDetectorCompat swipeListener = new GestureDetectorCompat(activity, new GestureDetector.SimpleOnGestureListener() {

    private class LeftRightFlingListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onDown(MotionEvent event) {
            return true;
        }

        @Override
        public boolean onFling(MotionEvent event1, MotionEvent event2, float velocityX, float velocityY) {
            if (Math.abs(velocityX) > 2 * Math.abs(velocityY)) {
                // flinging left to right (go one tab to the left)
                if (velocityX > 500 && currentTabIndex > 0) {
                    tabHost.setCurrentTab(currentTabIndex - 1);
                    return true;
                    // else flinging right to left (go one tab to the right)
                } else if (velocityX < -500 && currentTabIndex < categoryTreeIds.size() - 1) {
                    tabHost.setCurrentTab(currentTabIndex + 1);
                    return true;
                }
            }
            return super.onFling(event1, event2, velocityX, velocityY);
        }

    };
}

