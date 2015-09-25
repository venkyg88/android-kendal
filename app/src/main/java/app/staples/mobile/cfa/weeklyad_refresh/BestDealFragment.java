package app.staples.mobile.cfa.weeklyad_refresh;

import android.app.Activity;
import android.app.Fragment;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.crittercism.app.Crittercism;
import com.staples.mobile.common.access.Access;
import com.staples.mobile.common.access.easyopen.util.WeeklyAdImageUrlHelper;
import com.staples.mobile.common.analytics.Tracker;
import com.staples.mobile.common.shoplocal.api.ShopLocalApi;
import com.staples.mobile.common.shoplocal.models.DealList;

import app.staples.R;
import app.staples.mobile.cfa.MainActivity;
import app.staples.mobile.cfa.cart.CartApiManager;
import app.staples.mobile.cfa.widget.ActionBar;
import app.staples.mobile.cfa.widget.HorizontalDivider;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by Avinash Dodda.
 */
public class BestDealFragment extends Fragment implements View.OnClickListener{

    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private WeeklyAdListAdapter mAdapter;
    private ShopLocalApi shopLocalApi;
    private String storeId;

    private static final String ARG_STOREID = "storeId";

    public void setArguments(String storeId) {
        Bundle args = new Bundle();
        args.putString(ARG_STOREID, storeId);
        setArguments(args);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        shopLocalApi = Access.getInstance().getShopLocalAPi();
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        ActionBar.getInstance().setConfig(ActionBar.Config.BESTDEAL);
        Tracker.getInstance().trackStateForWeeklyAdClass(); // Analytics
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Crittercism.leaveBreadcrumb("WeeklyAdByCategoryFragment:onCreateView(): Displaying the Weekly Ad by Best Deals screen.");

        Bundle args = getArguments();
        if (args != null) {
            storeId = args.getString(ARG_STOREID);
        }

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_best_deal, container, false);

        mRecyclerView = (RecyclerView)rootView.findViewById(R.id.best_deal_items);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.addItemDecoration(new HorizontalDivider(getActivity()));
        mAdapter = new WeeklyAdListAdapter(getActivity());
        mRecyclerView.setAdapter(mAdapter);

        mAdapter.setOnClickListener(this);

        getWeeklyAdDeals();

        return rootView;
    }

    private void getWeeklyAdDeals() {
        if(shopLocalApi == null)return;
        shopLocalApi.getDeals(storeId, new Callback<DealList>() {
            @Override
            public void success(DealList dealList, Response response) {
                mAdapter.fillBestDealData(dealList.getDealResultsList());
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });
    }

    @Override
    public void onClick(View v) {
        Activity activity = getActivity();
        final Resources res = activity.getResources();
        Object tag;
        switch (v.getId()) {
            case R.id.weekly_ad_list_view: // go to sku page
                tag = v.getTag();
                if (tag instanceof WeeklyAdListAdapter.Item) {
                    WeeklyAdListAdapter.Item item = (WeeklyAdListAdapter.Item) tag;

                    // if in-store item, open expanded image of the ad, otherwise open sku page
                    if (item.inStoreOnly || item.buyNow == null) {
                        String imageUrl = WeeklyAdImageUrlHelper.getUrl(
                                (int) res.getDimension(R.dimen.weekly_ad_image_height),
                                (int) res.getDimension(R.dimen.weekly_ad_image_width),
                                item.imageUrl);
                        ((MainActivity) getActivity()).selectInStoreWeeklyAd(item.description, item.finalPrice, item.unit, item.literal, imageUrl, item.inStoreOnly);
                    } else {
                        // open SKU page
                        ((MainActivity) getActivity()).selectSkuItem(item.title, item.identifier, false);
                    }
                }
                break;
            case R.id.action: // add to cart
                tag = v.getTag();
                if (tag instanceof WeeklyAdListAdapter.Item) {
                    WeeklyAdListAdapter.Item item = (WeeklyAdListAdapter.Item) tag;
                    new AddToCart(item);
                }
                break;
        }
    }

    private class AddToCart implements CartApiManager.CartRefreshCallback {
        private WeeklyAdListAdapter.Item item;

        private AddToCart(WeeklyAdListAdapter.Item item) {
            MainActivity activity = (MainActivity) getActivity();

            this.item = item;
            item.busy = true;
            activity.swallowTouchEvents(true);

            mAdapter.notifyDataSetChanged();
            CartApiManager.addItemToCart(item.identifier, 1, this);
        }

        @Override
        public void onCartRefreshComplete(String errMsg) {
            Activity activity = getActivity();
            if (!(activity instanceof MainActivity)) return;

            ((MainActivity) activity).swallowTouchEvents(false);
            item.busy = false;
            mAdapter.notifyDataSetChanged();

            // if success
            if (errMsg == null) {
                ActionBar.getInstance().setCartCount(CartApiManager.getCartTotalItems());
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
}
