package app.staples.mobile.cfa.weeklyad_refresh;

import android.app.Activity;
import android.app.Fragment;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.TabHost;

import com.crittercism.app.Crittercism;
import com.staples.mobile.common.access.Access;
import com.staples.mobile.common.access.easyopen.api.EasyOpenApi;
import com.staples.mobile.common.access.easyopen.model.ApiError;
import com.staples.mobile.common.access.easyopen.model.weeklyad.Collection;
import com.staples.mobile.common.access.easyopen.model.weeklyad.Content;
import com.staples.mobile.common.access.easyopen.model.weeklyad.Data;
import com.staples.mobile.common.access.easyopen.model.weeklyad.WeeklyAd;
import com.staples.mobile.common.access.easyopen.util.WeeklyAdImageUrlHelper;
import com.staples.mobile.common.analytics.Tracker;

import java.util.ArrayList;
import java.util.List;

import app.staples.R;
import app.staples.mobile.cfa.MainActivity;
import app.staples.mobile.cfa.cart.CartApiManager;
import app.staples.mobile.cfa.widget.ActionBar;
import app.staples.mobile.cfa.widget.HorizontalDivider;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class WeeklyAdListFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = WeeklyAdListFragment.class.getSimpleName();

    private static final String STOREID = "storeid";
    private static final String CATEGORYTREEIDS = "categoryTreeIds";
    private static final String TITLES = "titles";
    private static final String TABINDEX = "tabIndex";

    private String storeId;
    private List<String> categoryTreeIds;
    private List<String> titles;
    private int currentTabIndex;

    private WeeklyAdListAdapter adapter;
    private TabHost tabHost;
    private HorizontalScrollView tabScrollView;

    public void setArguments(String storeId, int currentTabIndex,
                             ArrayList<String> categoryTreeIds, ArrayList<String> titles) {
        Bundle args = new Bundle();
        args.putString(STOREID, storeId);
        args.putStringArrayList(CATEGORYTREEIDS, categoryTreeIds);
        args.putStringArrayList(TITLES, titles);
        args.putInt(TABINDEX, currentTabIndex);
        setArguments(args);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Crittercism.leaveBreadcrumb("WeeklyAdListFragment:onCreateView(): Displaying the Weekly Ad List screen.");

        Activity activity = getActivity();
        View view = inflater.inflate(R.layout.weekly_ad_list, container, false);

        Bundle args = getArguments();
        if (args!=null) {
            storeId = args.getString(STOREID);
            categoryTreeIds = args.getStringArrayList(CATEGORYTREEIDS);
            titles = args.getStringArrayList(TITLES);
            currentTabIndex = args.getInt(TABINDEX);
        }

        // set up recycler view
        final RecyclerView list = (RecyclerView) view.findViewById(R.id.weekly_ad_list_items);
        list.setHasFixedSize(true);
        list.setLayoutManager(new LinearLayoutManager(activity));
        list.addItemDecoration(new HorizontalDivider(activity));
        adapter = new WeeklyAdListAdapter(activity);
        adapter.setOnClickListener(this);
        list.setAdapter(adapter);

        // set up tabs
        tabHost = (TabHost) view.findViewById(android.R.id.tabhost);
        tabScrollView = (HorizontalScrollView)view.findViewById(R.id.tabs_scrollview);
        tabHost.setup();
        TabHost.TabContentFactory tabContentFactory = new TabHost.TabContentFactory() {
            @Override public View createTabContent(String tag) { return list; }
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

    private void getWeeklyAdListing() {
        Activity activity = getActivity();
      //  activity.showProgressIndicator();
        final int imageWidth = (int) activity.getResources().getDimension(R.dimen.weekly_ad_list_item_image_width);
        EasyOpenApi easyOpenApi = Access.getInstance().getEasyOpenApi(false);
        easyOpenApi.getWeeklyAdCategoryListing(storeId, categoryTreeIds.get(currentTabIndex),
                imageWidth, 1, 100,
                new Callback<WeeklyAd>() {
            @Override
            public void success(WeeklyAd weeklyAd, Response response) {
                Activity activity = getActivity();
                if (!(activity instanceof MainActivity)) return;

//                activity.hideProgressIndicator();
                adapter.clear();
                if (weeklyAd!=null) {
                    Content content = weeklyAd.getContent();
                    if (content!=null) {
                        Collection collection = content.getCollection();
                        if (collection!=null) {
                            List<Data> datas = collection.getData();
                            adapter.fill(datas);
                        }
                    }
                }
            }

            @Override
            public void failure(RetrofitError error) {
                Activity activity = getActivity();
                if (!(activity instanceof MainActivity)) return;

//                    activity.hideProgressIndicator();
                ((MainActivity) activity).showErrorDialog(ApiError.getErrorMessage(error));
            }
        });
    }

    private class AddToCart implements CartApiManager.CartRefreshCallback {
        private WeeklyAdListAdapter.Item item;

        private AddToCart(WeeklyAdListAdapter.Item item) {
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
                ((MainActivity) activity).showNotificationBanner(R.string.cart_updated_msg);
                Tracker.getInstance().trackActionForAddToCartFromWeeklyAd(CartApiManager.getCartProduct(item.identifier), 1);
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
        Activity activity = getActivity();
        final Resources res = activity.getResources();
        Object tag;
        switch(view.getId()) {
            case R.id.weekly_ad_list_view: // go to sku page
                tag = view.getTag();
                if (tag instanceof WeeklyAdListAdapter.Item) {
                    WeeklyAdListAdapter.Item item = (WeeklyAdListAdapter.Item) tag;

                    // if in-store item, open expanded image of the ad, otherwise open sku page
                    if (item.inStoreOnly || item.buyNow==null) {
                        String imageUrl = WeeklyAdImageUrlHelper.getUrl(
                                (int) res.getDimension(R.dimen.weekly_ad_image_height),
                                (int) res.getDimension(R.dimen.weekly_ad_image_width),
                                item.imageUrl);
                        ((MainActivity) getActivity()).selectInStoreWeeklyAd(item.description, item.finalPrice, item.unit, item.literal, imageUrl, item.inStoreOnly);
                    } else {
                        // open SKU page
                        ((MainActivity) getActivity()).selectSkuItem(item.title, item.identifier, false);
                        // TODO: the following hasn't been specified, but I expect it's coming
                        // Tracker.getInstance().trackActionForWeeklyAdSelection(adapter.getItemPosition(item), 1); // analytics
                    }
                }
                break;
            case R.id.action: // add to cart
                tag = view.getTag();
                if (tag instanceof WeeklyAdListAdapter.Item) {
                    WeeklyAdListAdapter.Item item = (WeeklyAdListAdapter.Item) tag;
                    new AddToCart(item);
                }
                break;
        }
    }

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

