package app.staples.mobile.cfa.weeklyad_refresh;

import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.crittercism.app.Crittercism;
import com.squareup.picasso.Picasso;
import com.staples.mobile.common.access.Access;
import com.staples.mobile.common.access.channel.model.store.Obj;
import com.staples.mobile.common.access.channel.model.store.StoreData;
import com.staples.mobile.common.access.channel.model.store.StoreQuery;
import com.staples.mobile.common.access.easyopen.api.EasyOpenApi;
import com.staples.mobile.common.access.easyopen.model.ApiError;
import com.staples.mobile.common.access.easyopen.model.weeklyad.Collection;
import com.staples.mobile.common.access.easyopen.model.weeklyad.Content;
import com.staples.mobile.common.access.easyopen.model.weeklyad.Data;
import com.staples.mobile.common.access.easyopen.model.weeklyad.WeeklyAd;
import com.staples.mobile.common.access.easyopen.util.WeeklyAdImageUrlHelper;
import com.staples.mobile.common.analytics.Tracker;
import com.staples.mobile.common.shoplocal.api.ShopLocalApi;
import com.staples.mobile.common.shoplocal.models.CategoryList;
import com.staples.mobile.common.shoplocal.models.DealList;
import com.staples.mobile.common.shoplocal.models.DealResults;
import com.staples.mobile.common.shoplocal.models.PromotionPageCategoryList;
import com.staples.mobile.common.shoplocal.models.PromotionPageCategoryResults;
import com.staples.mobile.common.shoplocal.models.PromotionPagesList;
import com.staples.mobile.common.shoplocal.models.PromotionPagesResults;
import com.staples.mobile.common.shoplocal.models.PromotionsList;

import java.util.List;

import app.staples.R;
import app.staples.mobile.cfa.DrawerItem;
import app.staples.mobile.cfa.MainActivity;
import app.staples.mobile.cfa.cart.CartApiManager;
import app.staples.mobile.cfa.location.LocationFinder;
import app.staples.mobile.cfa.weeklyad.WeeklyAdInStoreFragment;
import app.staples.mobile.cfa.widget.ActionBar;
import app.staples.mobile.cfa.widget.HorizontalDivider;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class WeeklyAdFragment extends Fragment implements View.OnClickListener, ViewPager.OnPageChangeListener {

    private ShopLocalApi shopLocalApi;
    ImageView dealImage;
    TextView dealTitle;
    TextView dealPrice;
    TextView dealExpiry;
    LinearLayout dealLayout;
    ImageView promotionsBtn;
    ImageView categoryBtn;
    ImageView storeBtn;
    TextView storeNameTv;
    Button viewAllBtn;

    private RecyclerView mRecyclerView;
    private WeeklyAdCategoryAdapter mAdapter;
    private WeeklyAdListAdapter pAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    ViewPager viewPager;
    WeeklyAdImageAdapter adapter;

    private String storeId; // special store id required for weekly ad service
    private String storeNo; // storeNo available via store finder
    private String city;

    private static final String ARG_STORENO = "storeNo";
    private static final String ARG_STOREID = "storeId";

    private static final String DEFAULT_STORE_NO = "0349";

    List<DealResults> dealResultsList;
    List<PromotionPagesResults> promotionPagesResults;

    public void setArguments(String storeNo, String storeId /*, String city, String address*/) {
        Bundle args = new Bundle();
        args.putString(ARG_STORENO, storeNo);
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
        ActionBar.getInstance().setConfig(ActionBar.Config.WEEKLYAD);
        Tracker.getInstance().trackStateForWeeklyAdClass(); // Analytics
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Crittercism.leaveBreadcrumb("WeeklyAdByCategoryFragment:onCreateView(): Displaying the Weekly Ad by Promotions screen.");
        MainActivity activity = (MainActivity)getActivity();

        Bundle args = getArguments();
        if (args != null) {  // note that there will likely be a title arg even when no storeNo, so storeNo may still be null when args is not
            storeNo = args.getString(ARG_STORENO);
            storeId = args.getString(ARG_STOREID);
        }

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_weekly_ad, container, false);
        dealLayout = (LinearLayout)rootView.findViewById(R.id.deal_layout);
        dealImage = (ImageView)rootView.findViewById(R.id.dealImage);
        dealTitle = (TextView)rootView.findViewById(R.id.dealTitle);
        dealPrice = (TextView)rootView.findViewById(R.id.dealPricing);
        dealExpiry = (TextView)rootView.findViewById(R.id.dealExpiry);
        storeBtn = (ImageView) rootView.findViewById(R.id.goto_here_switch);
        categoryBtn = (ImageView) rootView.findViewById(R.id.list_switch);
        promotionsBtn = (ImageView) rootView.findViewById(R.id.promotions_switch);
        storeNameTv = (TextView) rootView.findViewById(R.id.store_name);
        viewAllBtn = (Button) rootView.findViewById(R.id.dealViewAll);

        promotionsBtn.setOnClickListener(this);
        categoryBtn.setOnClickListener(this);
        storeBtn.setOnClickListener(this);
        viewAllBtn.setOnClickListener(this);

        mRecyclerView = (RecyclerView)rootView.findViewById(R.id.weekly_ad_category_items);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.addItemDecoration(new HorizontalDivider(getActivity()));
        mAdapter = new WeeklyAdCategoryAdapter(getActivity());

        dealLayout.setVisibility(View.GONE);
        promotionsBtn.setVisibility(View.GONE);

        viewPager = (ViewPager) rootView.findViewById(R.id.promotion_images);
        adapter = new WeeklyAdImageAdapter(getActivity());
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(this);

        pAdapter = new WeeklyAdListAdapter(getActivity());
        pAdapter.setOnClickListener(this);

        if(!TextUtils.isEmpty(storeId)){
            getWeeklyAdStoreAndData();
        } else{
            // if store info avail
            if (!TextUtils.isEmpty(storeNo)) {
                getWeeklyAdStoreAndData();
            } else {
                // otherwise get store info from postal code
                LocationFinder finder = LocationFinder.getInstance(activity);
                String postalCode = finder.getPostalCode();
                if (!TextUtils.isEmpty(postalCode)) {
                    Access.getInstance().getChannelApi(false).storeLocations(postalCode, new StoreInfoCallback());
                } else {
                    storeNo = DEFAULT_STORE_NO;
                    getWeeklyAdStoreAndData();
                }
            }
        }

        return rootView;
    }

    private void getWeeklyAdDeals() {
        if(shopLocalApi == null)return;
        shopLocalApi.getDeals(storeId, new Callback<DealList>() {
            @Override
            public void success(DealList dealList, Response response) {
                dealResultsList = dealList.getDealResultsList();
                int random = (int)(Math.random() * dealResultsList.size());
                DealResults deal = dealResultsList.get(random);
                Picasso.with(getActivity())
                        .load(deal.getImageLocation())
                        .into(dealImage);
                dealTitle.setText(deal.getTitle());
                dealPrice.setText(deal.getDeal());
                String saleStart = deal.getSaleStartDateString().substring(0, deal.getSaleStartDateString().indexOf(" "));
                String saleEnd = deal.getSaleEndDateString().substring(0, deal.getSaleEndDateString().indexOf(" "));
                dealExpiry.setText("Valid " + saleStart + " - " + saleEnd);
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });
    }

    private void getWeeklyAdStoreAndData() {
        MainActivity activity = (MainActivity) getActivity();
        if (activity==null) return;

        final EasyOpenApi easyOpenApi = Access.getInstance().getEasyOpenApi(false);
        easyOpenApi.getWeeklyAdStore(Integer.parseInt(storeNo), new Callback<WeeklyAd>() {
            @Override
            public void success(WeeklyAd weeklyAdStore, Response response) {
                MainActivity activity = (MainActivity) getActivity();
                if (activity == null) return;

                if (weeklyAdStore != null) {
                    Content content = weeklyAdStore.getContent();
                    if (content != null) {
                        Collection collection = content.getCollection();
                        if (collection != null) {
                            List<Data> datas = collection.getData();
                            if (datas != null && datas.size() > 0) {
                                Data data = datas.get(0);
                                storeId = String.valueOf(data.getStoreid());
                                mAdapter.setStoreId(storeId);
                                city = data.getCity();
                                storeNameTv.setText(city);

                                getWeeklyAdPromotions();
                                return;
                            }
                        }
                    }
                }

                activity.showErrorDialog(R.string.empty);
            }

            @Override
            public void failure(RetrofitError error) {
                MainActivity activity = (MainActivity) getActivity();
                if (activity == null) return;

                activity.showErrorDialog(ApiError.getErrorMessage(error));
            }
        });
    }

    private void getWeeklyAdCategories() {
        if(shopLocalApi == null)return;
        shopLocalApi.getCategories(storeId, new Callback<CategoryList>() {
            @Override
            public void success(CategoryList categoryList, Response response) {
                mAdapter.clear();
                mRecyclerView.setAdapter(mAdapter);
                mAdapter.fill(categoryList.getCategoryResultsList());
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });
    }

    private void getWeeklyAdPromotions() {
        if(shopLocalApi == null)return;
        shopLocalApi.getPromotions(storeId, new Callback<PromotionsList>() {
            @Override
            public void success(PromotionsList promotionsList, Response response) {
                String promotionCode = promotionsList.getPromotionsResultsList().get(0).getPromotionCode();
                shopLocalApi.getPromotionPages(storeId, promotionCode, "1000", new Callback<PromotionPagesList>() {
                    @Override
                    public void success(PromotionPagesList promotionPagesList, Response response) {
                        promotionPagesResults = promotionPagesList.getPromotionPagesResultsList();
                        for (PromotionPagesResults result : promotionPagesList.getPromotionPagesResultsList()) {
                            adapter.add(result.getImageLocation());
                        }
                        adapter.notifyDataSetChanged();
                        getWeeklyAdPromotionCategories(promotionPagesResults.get(0).getPromotionPageId());
                    }

                    @Override
                    public void failure(RetrofitError error) {

                    }
                });
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });
    }

    public void getWeeklyAdPromotionCategories(String promotionPageId) {
        if(shopLocalApi == null)return;
        shopLocalApi.getPromotionPageListings(storeId, promotionPageId, new Callback<PromotionPageCategoryList>() {
            @Override
            public void success(PromotionPageCategoryList promotionPageCategoryList, Response response) {
                pAdapter.clear();
                mRecyclerView.setAdapter(pAdapter);
                pAdapter.fillPromotionData(promotionPageCategoryList.getPromotionPageCategoryResultsList());
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
        switch (v.getId()){
            case R.id.goto_here_switch:
                if(activity == null) return;
                ((MainActivity)activity).selectStoreFragment();
                break;
            case R.id.list_switch:
                showCategories();
                break;
            case R.id.promotions_switch:
               showPromotionPages();
                break;
            case R.id.weekly_ad_list_view: // go to sku page
                tag = v.getTag();
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
            case R.id.dealViewAll: // view all best deals
                BestDealFragment fragment = new BestDealFragment();
                fragment.setArguments(storeId);
                ((MainActivity) getActivity()).selectFragment("", fragment, MainActivity.Transition.RIGHT);

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

    private void showCategories() {
        getWeeklyAdDeals();
        getWeeklyAdCategories();
        viewPager.setVisibility(View.GONE);
        dealLayout.setVisibility(View.VISIBLE);
        categoryBtn.setVisibility(View.GONE);
        promotionsBtn.setVisibility(View.VISIBLE);
    }

    private void showPromotionPages() {
        mRecyclerView.setAdapter(pAdapter);
        viewPager.setVisibility(View.VISIBLE);
        dealLayout.setVisibility(View.GONE);
        categoryBtn.setVisibility(View.VISIBLE);
        promotionsBtn.setVisibility(View.GONE);
    }

    private class StoreInfoCallback implements Callback<StoreQuery> {
        @Override
        public void success(StoreQuery storeQuery, Response response) {
            MainActivity activity = (MainActivity) getActivity();
            if (activity==null) return;

            List<StoreData> storeData = storeQuery.getStoreData();
            // if there are any nearby stores
            if (storeData != null && !storeData.isEmpty()) {

                // Get store location
                Obj storeObj = storeData.get(0).getObj();
                storeNo = storeObj.getStoreNumber();
            }
            // use default if no store result
            else {
                storeNo = DEFAULT_STORE_NO;
            }

            getWeeklyAdStoreAndData();
        }

        @Override
        public void failure(RetrofitError retrofitError) {
            MainActivity activity = (MainActivity) getActivity();
            if (activity==null) return;

            activity.hideProgressIndicator();
            activity.showErrorDialog(ApiError.getErrorMessage(retrofitError));
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        getWeeklyAdPromotionCategories(promotionPagesResults.get(position).getPromotionPageId());
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }
}
